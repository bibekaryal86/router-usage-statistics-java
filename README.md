# router-usage-statistics-java

The project started as a POC for web scraping using JSoup where the objective as to be able to scrape internet data 
usage from router page. The reason for need being that the router's UI was only displaying the last three days of data 
usage only.

The app is one of the two repos used to save-retrieve-display data:
* https://github.com/bibekaryal86/router-usage-statistics-java (save/retrieve data) (this)
* https://github.com/bibekaryal86/router-usage-statistics-spa (view data)

In local network only, the App logs into Asus Router, gets daily data usage and saves to MongoDb repository on a 
scheduled time.

In local network and in the cloud, the app provides a way to query the saved data and present the data in HTML or JSON
format. The HTML data can be viewed directly from the browser, or the JSON data can be consumed by other front-end
application (SPA) for display purposes.

There are added logic to check whether data updated or not, and send email using MailJet if needed. Also, to test, SMS can be sent using Sinch.

The App does not use any frameworks or any view layer technology. The App runs on embedded Jetty, uses Quartz, JSoup, 
MongoDb driver, and MailJet API. The obvious choice would be to use Springboot, however, decided against it in order to keep the
app's JVM memory footprint as low as possible so that it can continuously run in Google Cloud Platform's free tier.

MongoDb supports BigDecimal objects, should have used that for dataUpload/dataDownload/dataTotals instead of String to 
avoid all the conversions during calculations. And, of course, Java should not be used to create html code!

When running the app, the following environment variables are needed:
* port (for running locally only, if preferred)
* profile (eg: local, docker, cloud)
* time zone (eg: TZ=America/Denver)
* mongodb database name
* mongodb username
* mongodb password
* router login username (for running locally only)
* router login password (for running locally only)
* email address to send/receive (currently same email used to send/receive)
* email sender/receiver name (current same name used to send/receive)
* MailJet API Key (public)
* MailJet API Key (private)
  Additional environment variables for Sinch SMS
* phone number provided by Sinch to send SMS
* phone number to receive SMS
* Sinch Plan Id
* Sinch Auth Token

These variables are included in app.yaml for GCP, Dockerfile or docker-compose.yml for docker, and should be included in the command when running the app locally.

For example: java -jar -DPORT=7001 -DPROFILE=docker -D"TZ=America/Denver" -DDBNAME=mongodb_database_name -DDBUSR=mongodb_username -DDBPWD=mongodb_password -DJSUSR=router_login_username -D"JSPWD=router login password" -DAPI_KEY_PUB=mailjet_public_key -DAPI_KEY_PRV=mailjet_private_key -D"EMAIL=whateversoandso@gmail.com" -D"NAME=Whatever SoAndSo (MAILJET)" -DSENDER=11234567890 -DRECEIVER=10987654321 -DSINCH_PLAN=plan_id -DSINCH_TOKEN=the_token SOMETHING.jar

The app is deployed here:
* https://routerstat.appspot.com/ (for HTML display)
* https://routerstat.appspot.com/?toJson=true (for JSON response)


