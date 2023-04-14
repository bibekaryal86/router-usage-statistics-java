package router.usage.statistics.java.service;

import static java.math.RoundingMode.UP;
import static java.time.ZoneId.of;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.jsoup.Jsoup.parse;
import static router.usage.statistics.java.connector.Connector.*;
import static router.usage.statistics.java.util.Util.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import router.usage.statistics.java.model.Model;
import router.usage.statistics.java.model.ModelResponse;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Service {

  private static Map<String, String> cookies = null;
  private static String previousTotalData = "";
  private static int previousTotalDataFailCount = 0;
  private static String previousDataUpdatedAt = "";
  private static String previousDataCheckedAt = "";

  public static String returnDataCheck() {
    return String.format(
        "{\"check\": {\"previousTotalData\": \"%s\",\"previousTotalDataFailCount\": \"%s\","
            + "\"previousDataUpdatedAt\": \"%s\",\"previousDataCheckedAt\": \"%s\"}}",
        previousTotalData,
        previousTotalDataFailCount,
        previousDataUpdatedAt,
        previousDataCheckedAt);
  }

  public static void insertDataUsages() {
    log.info("Start Insert Data Usages");

    LocalDateTime localDateTime = LocalDateTime.now();
    Model modelJsoup = getWanTraffic(localDateTime);
    Model modelMongo = retrieveDataUsage(localDateTime);

    dailyDataInsert(modelJsoup, modelMongo);

    log.info("Finish Insert Data Usages");
  }

  public static Set<String> retrieveUniqueDatesOnly() {
    List<String> uniqueDates = retrieveUniqueDates();
    return uniqueDates.stream()
        .map(uniqueDate -> uniqueDate.substring(0, 7))
        .sorted(reverseOrder())
        .collect(toCollection(LinkedHashSet::new));
    // unsorted.stream().sorted(nullsLast(comparing(ClassName::getMethodName,
    // nullsLast(naturalOrder())))).collect(toList());   // NOSONAR
  }

  public static List<Model> retrieveDataUsages(List<String> years, List<String> months) {
    List<Model> modelList = retrieveDailyDataUsage(years, null);

    if (months == null || months.isEmpty()) {
      return modelList;
    } else {
      return filterDataUsageListByMonth(modelList, months);
    }
  }

  public static Model retrieveDataUsage(LocalDateTime localDateTime) {
    String date = getModelClassDate(localDateTime);
    List<Model> modelList = retrieveDailyDataUsage(null, date);

    if (modelList.isEmpty()) {
      return null;
    } else {
      return modelList.get(0);
    }
  }

  public static Model calculateTotalDataUsage(List<Model> modelList) {
    BigDecimal totalUploads = new BigDecimal("0.00");
    BigDecimal totalDownloads = new BigDecimal("0.00");
    BigDecimal totalTotals = new BigDecimal("0.00");

    for (Model model : modelList) {
      totalUploads = totalUploads.add(new BigDecimal(model.getDataUpload()));
      totalDownloads = totalDownloads.add(new BigDecimal(model.getDataDownload()));
      totalTotals = totalTotals.add(new BigDecimal(model.getDataTotal()));
    }

    return Model.builder()
        .dataUpload(totalUploads.toString())
        .dataDownload(totalDownloads.toString())
        .dataTotal(totalTotals.toString())
        .build();
  }

  public static ModelResponse getModelResponse(
      Set<String> yearMonthSet, List<Model> modelList, Model modelTotal) {
    Model formattedModelTotal =
        Model.builder()
            .dataUpload(getFormattedData(modelTotal.getDataUpload()))
            .dataDownload(getFormattedData(modelTotal.getDataDownload()))
            .dataTotal(getFormattedData(modelTotal.getDataTotal()))
            .build();

    List<Model> formattedModelList =
        modelList.stream()
            .map(
                model ->
                    Model.builder()
                        .id(model.getId())
                        .date(model.getDate())
                        .year(model.getYear())
                        .day(model.getDay())
                        .dataUpload(getFormattedData(model.getDataUpload()))
                        .dataDownload(getFormattedData(model.getDataDownload()))
                        .dataTotal(getFormattedData(model.getDataTotal()))
                        .build())
            .collect(toList());

    return ModelResponse.builder()
        .modelList(formattedModelList)
        .yearMonthSet(yearMonthSet)
        .modelTotal(formattedModelTotal)
        .build();
  }

  public static String getFormattedData(String dataInput) {
    return ""
        + new DecimalFormat("#,##0.00")
            .format(
                new BigDecimal(dataInput)
                    .divide(new BigDecimal("1024"), 2, UP)
                    .divide(new BigDecimal("1024"), 2, UP)
                    .divide(new BigDecimal("1024"), 2, UP))
        + " GB";
  }

  private static Model getWanTraffic(LocalDateTime localDateTime) {
    log.info("Start Get Wan Traffic");

    if (!isLoggedIn()) {
      login();
    }

    Connection.Response wanTrafficResponse = wanTraffic();

    if (wanTrafficResponse == null) {
      return null;
    }

    try {
      Document document = parse(wanTrafficResponse.parse().html());

      if (!isLoggedIn(document)) {
        login();
        wanTrafficResponse = wanTraffic();

        if (wanTrafficResponse == null) {
          return null;
        } else {
          document = parse(wanTrafficResponse.parse().html());
        }
      }

      return convertDataUsage(document.body().text(), localDateTime);
    } catch (Exception ex) {
      log.error("Get Wan Traffic Error", ex);
      return null;
    }
  }

  private static void dailyDataInsert(Model modelJsoup, Model modelMongo) {
    log.info("Daily Data Insert: {} | {}", modelJsoup, modelMongo);

    if (modelJsoup == null) {
      log.error("Data Usage Jsoup to Insert is Null");
    } else {
      if (modelMongo == null) {
        previousDataUpdatedAt = insertDailyDataUsage(modelJsoup, previousDataUpdatedAt);
      } else {
        previousDataUpdatedAt =
            updateDailyDataUsage(modelJsoup, modelJsoup.getDate(), previousDataUpdatedAt);
      }
    }
  }

  private static void login() {
    Map<String, String> formData = new HashMap<>();
    formData.put("group_id", "");
    formData.put("action_mode", "");
    formData.put("action_script", "");
    formData.put("action_wait", "");
    formData.put("current_page", "Main_Login.asp");
    formData.put("next_page", "index.asp");
    formData.put("login_captcha", "");

    Connection.Response connectionResponse =
        connectionResponse(
            LOGIN_ACTION_URL,
            new HashMap<>(),
            LOGIN_URL,
            formData,
            Connection.Method.POST,
            USER_AGENT);

    if (connectionResponse != null) {
      cookies = connectionResponse.cookies();
    }
  }

  private static String getModelClassDate(LocalDateTime localDateTime) {
    return localDateTime.getHour() == 0
        ? localDateTime.toLocalDate().minusDays(1).toString()
        : localDateTime.toLocalDate().toString();
  }

  private static String getModelClassYear(LocalDateTime localDateTime) {
    return localDateTime.getHour() == 0
            && localDateTime.getMonthValue() == 1
            && localDateTime.getDayOfMonth() == 1
        ? String.valueOf(localDateTime.toLocalDate().minusDays(1).getYear())
        : String.valueOf(localDateTime.toLocalDate().getYear());
  }

  private static String getModelClassDay(LocalDateTime localDateTime) {
    return localDateTime.getHour() == 0
        ? localDateTime.toLocalDate().minusDays(1).getDayOfWeek().toString()
        : localDateTime.toLocalDate().getDayOfWeek().toString();
  }

  private static boolean isLoggedIn() {
    return cookies != null && cookies.containsKey("asus_token");
  }

  private static boolean isLoggedIn(Document document) {
    try {
      Element head = document.head();
      Elements scripts = head.select("script");
      return scripts.isEmpty();
    } catch (Exception ex) {
      log.error("Is Logged In Error", ex);
      return false;
    }
  }

  private static Connection.Response wanTraffic() {
    Map<String, String> formData = new HashMap<>();
    formData.put("client", "all");
    formData.put("mode", "hour");
    formData.put("dura", "24");
    formData.put("date", getShorterDate());
    formData.put("_", getLongerDate());

    return connectionResponse(
        GET_TRAFFIC_WAN_URL,
        cookies,
        TRAFFIC_ANALYZER_URL,
        formData,
        Connection.Method.GET,
        USER_AGENT);
  }

  private static Model convertDataUsage(String bodyTexts, LocalDateTime localDateTime) {
    int beginIndex = bodyTexts.indexOf("[[");
    int endIndex = bodyTexts.indexOf("]]");
    bodyTexts = bodyTexts.substring(beginIndex + 1, endIndex).replace("[", "").replace(" ", "");
    List<String> bodyTextList = asList(bodyTexts.split("],"));
    reverse(bodyTextList);

    int numberOfHours =
        localDateTime.getHour() == 0 ? bodyTextList.size() : localDateTime.getHour();
    BigDecimal upload = new BigDecimal("0.00");
    BigDecimal download = new BigDecimal("0.00");

    for (int i = 0; i < numberOfHours; i++) {
      String[] bodyTextArr = bodyTextList.get(i).split(",");
      upload = upload.add(new BigDecimal(bodyTextArr[0]));
      download = download.add(new BigDecimal(bodyTextArr[1]));
    }

    BigDecimal total = upload.add(download);
    String date = getModelClassDate(localDateTime);
    String year = getModelClassYear(localDateTime);
    String day = getModelClassDay(localDateTime);

    return Model.builder()
        .date(date)
        .year(year)
        .day(day)
        .dataUpload(upload.toString())
        .dataDownload(download.toString())
        .dataTotal(total.toString())
        .build();
  }

  private static List<Model> filterDataUsageListByMonth(
      List<Model> modelList, List<String> months) {
    return modelList.stream()
        .filter(
            model ->
                months.stream()
                    .anyMatch(
                        month -> {
                          Matcher matcher = compile("-(\\w+)-").matcher(model.getDate());
                          return matcher.find() && month.equals(matcher.group(1));
                        }))
        .collect(toList());
  }

  public static void checkPreviousData() {
    String selected;
    LocalDateTime localDateTime = LocalDateTime.now(of(getSystemEnvProperty(TIME_ZONE)));
    previousDataCheckedAt = localDateTime.toString();
    int monthValue = localDateTime.getMonthValue();
    if (monthValue < 10) {
      selected = localDateTime.getYear() + "-0" + localDateTime.getMonthValue();
    } else {
      selected = localDateTime.getYear() + "-" + localDateTime.getMonthValue();
    }
    String[] selectedYearMonth = selected.split("-");
    List<String> selectedYear = singletonList(selectedYearMonth[0]);
    List<String> selectedMonth = singletonList(selectedYearMonth[1]);

    List<Model> modelList = retrieveDataUsages(selectedYear, selectedMonth);

    Model model =
        modelList.stream()
            .filter(model1 -> model1.getDate().equals(localDateTime.toLocalDate().toString()))
            .findFirst()
            .orElse(null);

    if (model != null) {
      checkAndSendEmail(localDateTime, model);
    } else {
      if (localDateTime.getHour() == 0) {
        log.info("Skip Count at Midnight: {}", localDateTime);
        previousTotalDataFailCount++;
      } else {
        String text = String.format("Model is NULL: %s", localDateTime);
        log.info(text);
        sendEmail(text);
        sendSms(text);
      }
    }
  }

  private static void checkAndSendEmail(LocalDateTime localDateTime, Model model) {
    boolean isPreviousTotalDataReset = false;
    if (previousTotalData.isEmpty() || localDateTime.getHour() == 1) {
      log.info(
          "Previous Total Data: {} | {} | {}",
          previousTotalData.isEmpty(),
          localDateTime.getHour(),
          localDateTime);
      previousTotalData = model.getDataTotal();
      previousTotalDataFailCount = 0;
      isPreviousTotalDataReset = true;
    }

    log.info(
        "{} | {} | {} | {} | {}",
        previousTotalData,
        previousTotalDataFailCount,
        isPreviousTotalDataReset,
        model.getDataTotal(),
        model.getId());

    if (model.getDataTotal().equals(previousTotalData) && !isPreviousTotalDataReset) {
      previousTotalDataFailCount++;

      if (previousTotalDataFailCount < 2) {
        log.info(
            "Previous Total Data Fail Count: {} | {}", previousTotalDataFailCount, localDateTime);
      } else {
        String text =
            String.format(
                "Data was not Updated: %s | %s", previousTotalDataFailCount, localDateTime);
        sendEmail(text);
        sendSms(text);
      }
    } else {
      previousTotalData = model.getDataTotal();
      previousTotalDataFailCount = 0;
    }
  }
}
