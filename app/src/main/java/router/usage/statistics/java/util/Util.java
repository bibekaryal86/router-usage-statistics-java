package router.usage.statistics.java.util;

public class Util {

    // Constants

    // provided at runtime
    public static final String SERVER_PORT = "PORT";
    public static final String ACTIVE_PROFILE = "PROFILE";
    public static final String TIME_ZONE = "TIME_ZONE";
    public static final String MONGODB_DATABASE = "DBNAME";
    public static final String MONGODB_USERNAME = "DBUSR";
    public static final String MONGODB_PASSWORD = "DBPWD";
    public static final String JSOUP_USERNAME = "JSUSR";
    public static final String JSOUP_PASSWORD = "JSPWD";
    public static final String MJ_APIKEY_PUBLIC = "API_KEY_PUB";
    public static final String MJ_APIKEY_PRIVATE = "API_KEY_PRV";
    public static final String MJ_SENDER_EMAIL = "EMAIL";
    public static final String MJ_SENDER_NAME = "NAME";

    // others
    public static final int SERVER_MAX_THREADS = 100;
    public static final int SERVER_MIN_THREADS = 20;
    public static final int SERVER_IDLE_TIMEOUT = 120;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, " +
            "like Gecko) Chrome/87.0.4280.88 Safari/537.36";
    public static final String LOGIN_ACTION_URL = "http://router.asus.com/login.cgi";
    public static final String LOGIN_URL = "http://router.asus.com/Main_Login.asp";
    public static final String GET_TRAFFIC_WAN_URL = "http://router.asus.com/getWanTraffic.asp";
    public static final String TRAFFIC_ANALYZER_URL = "http://router.asus.com/TrafficAnalyzer_Statistic.asp";
    public static final String MONGODB_URI = "mongodb+srv://%s:%s@%s.2sj3v.mongodb.net/<dbname>?retryWrites=true&w=majority";
    public static final String MONGODB_COLLECTION_NAME = "model_class";


    // Common

    public static String getSystemEnvProperty(String keyName) {
        return System.getProperty(keyName) != null ? System.getProperty(keyName) : System.getenv(keyName);
    }

    public static String getShorterDate() {
        return String.valueOf((System.currentTimeMillis() + 86400) / 1000);
    }

    public static String getLongerDate() {
        return String.valueOf(System.currentTimeMillis());
    }
}
