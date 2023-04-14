package router.usage.statistics.java;

import static router.usage.statistics.java.service.Service.insertDataUsages;
import static router.usage.statistics.java.util.Util.isNotCloudDeployment;

import lombok.extern.slf4j.Slf4j;
import router.usage.statistics.java.scheduler.SchedulerQuartz;
import router.usage.statistics.java.server.ServerJetty;

@Slf4j
public class App {

  public static void main(String[] args) throws Exception {
    log.info("Begin router-usage-statistics-java initialization...");
    new ServerJetty().start();
    new SchedulerQuartz().start();
    log.info("End router-usage-statistics-java initialization...");

    // run once when the app starts
    if (isNotCloudDeployment()) {
      insertDataUsages();
    }
  }
}
