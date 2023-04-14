package router.usage.statistics.java.connector;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.MongoClients.create;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static java.util.Base64.getEncoder;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.jsoup.Jsoup.connect;
import static router.usage.statistics.java.util.Util.*;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import router.usage.statistics.java.model.Model;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Connector {

  private static String dbName = null;
  private static String dbUsr = null;
  private static String dbPwd = null;
  private static String jsUsr = null;
  private static String jsPwd = null;
  private static String loginAuth = null;
  private static String emailSenderEmail = null;
  private static String emailSenderName = null;
  private static String emailPrivateKey = null;
  private static String emailPublicKey = null;
  private static String smsSenderNumber = null;
  private static String smsReceiverNumber = null;
  private static String smsPlanId = null;
  private static String smsAuthToken = null;

  private static void initJsoup() {
    if (jsUsr == null || jsPwd == null) {
      jsUsr = getSystemEnvProperty(JSOUP_USERNAME);
      jsPwd = getSystemEnvProperty(JSOUP_PASSWORD);
      loginAuth = loginAuthorization();
    }
  }

  private static void initMongo() {
    if (dbName == null || dbUsr == null || dbPwd == null) {
      dbName = getSystemEnvProperty(MONGODB_DATABASE);
      dbUsr = getSystemEnvProperty(MONGODB_USERNAME);
      dbPwd = getSystemEnvProperty(MONGODB_PASSWORD);
    }
  }

  private static void initEmail() {
    if (emailSenderEmail == null
        || emailSenderName == null
        || emailPrivateKey == null
        || emailPublicKey == null) {
      emailSenderEmail = getSystemEnvProperty(MJ_SENDER_EMAIL);
      emailSenderName = getSystemEnvProperty(MJ_SENDER_NAME);
      emailPrivateKey = getSystemEnvProperty(MJ_APIKEY_PRIVATE);
      emailPublicKey = getSystemEnvProperty(MJ_APIKEY_PUBLIC);
    }
  }

  private static void initSms() {
    if (smsSenderNumber == null
        || smsReceiverNumber == null
        || smsPlanId == null
        || smsAuthToken == null) {
      smsSenderNumber = getSystemEnvProperty(SINCH_SENDER);
      smsReceiverNumber = getSystemEnvProperty(SINCH_RECEIVER);
      smsPlanId = getSystemEnvProperty(SINCH_PLAN_ID);
      smsAuthToken = getSystemEnvProperty(SINCH_AUTH_TOKEN);
    }
  }

  private static String loginAuthorization() {
    return getEncoder().encodeToString(jsUsr.concat(":").concat(jsPwd).getBytes());
  }

  public static Connection.Response connectionResponse(
      String url,
      Map<String, String> cookies,
      String referrer,
      Map<String, String> data,
      Connection.Method connectionMethod,
      String userAgent) {
    try {
      initJsoup();
      data.put("login_authorization", loginAuth);

      return connect(url)
          .cookies(cookies)
          .referrer(referrer)
          .data(data)
          .method(connectionMethod)
          .userAgent(userAgent)
          .execute();
    } catch (Exception ex) {
      log.error("Connection Response Error: ", ex);
      return null;
    }
  }

  private static MongoClientSettings getMongoClientSettings() {
    initMongo();
    String mongodbUri = String.format(MONGODB_URI, dbUsr, dbPwd, dbName);
    ConnectionString connectionString = new ConnectionString(mongodbUri);
    CodecRegistry pojoCodecRegistry =
        fromProviders(PojoCodecProvider.builder().automatic(true).build());
    CodecRegistry codecRegistry = fromRegistries(getDefaultCodecRegistry(), pojoCodecRegistry);

    return MongoClientSettings.builder()
        .applyConnectionString(connectionString)
        .codecRegistry(codecRegistry)
        .build();
  }

  private static MongoCollection<Model> getMongoCollectionDataUsage(MongoClient mongoClient) {
    return mongoClient.getDatabase(dbName).getCollection(MONGODB_COLLECTION_NAME, Model.class);
  }

  public static String insertDailyDataUsage(Model model, String previousDataUpdatedAt) {
    log.info("Insert Daily Data Usage: {}", model);

    try (MongoClient mongoClient = create(getMongoClientSettings())) {
      MongoCollection<Model> mongoCollectionModelClass = getMongoCollectionDataUsage(mongoClient);
      mongoCollectionModelClass.insertOne(model);

      return LocalDateTime.now(ZoneId.of(getSystemEnvProperty(TIME_ZONE))).toString();
    } catch (Exception ex) {
      log.error("Insert Daily Data Usage Error", ex);
      return previousDataUpdatedAt;
    }
  }

  public static String updateDailyDataUsage(
      Model model, String date, String previousDataUpdatedAt) {
    log.info("Update Daily Data Usage: {} | {}", date, model);

    try (MongoClient mongoClient = create(getMongoClientSettings())) {
      MongoCollection<Model> mongoCollectionModelClass = getMongoCollectionDataUsage(mongoClient);

      Bson filter = eq("date", date);
      Bson update1 = set("data_download", model.getDataDownload());
      Bson update2 = set("data_upload", model.getDataUpload());
      Bson update3 = set("data_total", model.getDataTotal());
      Bson update = combine(update1, update2, update3);
      FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(AFTER);

      mongoCollectionModelClass.findOneAndUpdate(filter, update, options);

      return LocalDateTime.now(ZoneId.of(getSystemEnvProperty(TIME_ZONE))).toString();
    } catch (Exception ex) {
      log.error("Update Daily Data Usage Error", ex);
      return previousDataUpdatedAt;
    }
  }

  public static List<Model> retrieveDailyDataUsage(List<String> years, String date) {
    List<Model> modelList = new ArrayList<>();

    try (MongoClient mongoClient = create(getMongoClientSettings())) {
      MongoCollection<Model> mongoCollectionModelClass = getMongoCollectionDataUsage(mongoClient);

      FindIterable<Model> findIterableModelClass;

      if (date == null) {
        findIterableModelClass =
            mongoCollectionModelClass.find(in("year", years), Model.class).sort(descending("date"));
      } else {
        findIterableModelClass = mongoCollectionModelClass.find(in("date", date), Model.class);
      }

      findIterableModelClass.forEach(modelList::add);
    } catch (Exception ex) {
      log.error("Retrieve Daily Data Usage Error", ex);
    }

    return modelList;
  }

  public static List<String> retrieveUniqueDates() {
    List<String> dateList = new ArrayList<>();

    try (MongoClient mongoClient = create(getMongoClientSettings())) {
      MongoCollection<Model> mongoCollectionModelClass = getMongoCollectionDataUsage(mongoClient);
      DistinctIterable<String> distinctIterableString =
          mongoCollectionModelClass.distinct("date", String.class);
      distinctIterableString.forEach(dateList::add);
    } catch (Exception ex) {
      log.error("Retrieve Unique Dates Error", ex);
    }

    return dateList;
  }

  private static MailjetClient getMailjetClient() {
    return new MailjetClient(
        ClientOptions.builder().apiKey(emailPublicKey).apiSecretKey(emailPrivateKey).build());
  }

  public static void sendEmail(String text) {
    initEmail();
    log.info("Send Email Request: {}", text);

    try {
      MailjetRequest request =
          new MailjetRequest(Emailv31.resource)
              .property(
                  Emailv31.MESSAGES,
                  new JSONArray()
                      .put(
                          new JSONObject()
                              .put(
                                  Emailv31.Message.FROM,
                                  new JSONObject()
                                      .put("Email", emailSenderEmail)
                                      .put("Name", emailSenderName))
                              .put(
                                  Emailv31.Message.TO,
                                  new JSONArray()
                                      .put(
                                          new JSONObject()
                                              .put("Email", emailSenderEmail)
                                              .put("Name", emailSenderName)))
                              .put(
                                  Emailv31.Message.SUBJECT,
                                  "ALERT! Internet Router Usage Statistics!!!")
                              .put(Emailv31.Message.TEXTPART, text)
                              .put(Emailv31.Message.CUSTOMID, UUID.randomUUID().toString())));

      MailjetResponse response = getMailjetClient().post(request);

      if (response.getStatus() == 200) {
        log.info("Send Email Response Success: {}", text);
      } else {
        log.info("Send Email Response Failure: {} | {}", text, response.getData());
      }
    } catch (Exception ex) {
      log.error("Send Email Error: {}", text, ex);
    }
  }

  public static void sendSms(String text) {
    initSms();
    log.info("Send Sms Request: {}", text);

    try {
      HttpClient httpClient = HttpClient.newBuilder().build();

      // copied from sinch's documentation
      // but not the way to do it (using model is the way to go)
      String payload =
          String.join(
              "\n",
              "{",
              " \"from\": \"" + smsSenderNumber + "\",",
              " \"to\": [",
              "  \"" + smsReceiverNumber + "\"",
              " ],",
              " \"body\": \"" + text + "\"",
              "}");

      HttpRequest request =
          HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(payload))
              .uri(
                  URI.create(
                      String.format("https://us.sms.api.sinch.com/xms/v1/%s/batches", smsPlanId)))
              .header("Content-Type", "application/json")
              .header("Authorization", String.format("Bearer %s", smsAuthToken))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        log.info("Send Sms Response Success: {}", text);
      } else {
        log.info("Send Sms Response Failure: {} | {}", text, response.body());
      }
    } catch (Exception ex) {
      log.error("Send Sms Error: {}", text, ex);

      if (ex instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
