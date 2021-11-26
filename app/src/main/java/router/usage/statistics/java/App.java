package router.usage.statistics.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.usage.statistics.java.scheduler.SchedulerQuartz;
import router.usage.statistics.java.server.ServerJetty;

import static router.usage.statistics.java.service.Service.insertDataUsages;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        log.info("Begin router-usage-statistics-java initialization...");
        new ServerJetty().start();
        new SchedulerQuartz().start();
        log.info("End router-usage-statistics-java initialization...");

        // run once when the app runs
        insertDataUsages();
    }
}
