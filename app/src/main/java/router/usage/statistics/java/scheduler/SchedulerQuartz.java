package router.usage.statistics.java.scheduler;

import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.of;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.StdSchedulerFactory.getDefaultScheduler;
import static router.usage.statistics.java.connector.Connector.sendEmail;
import static router.usage.statistics.java.util.Util.isNotCloudDeployment;

import java.sql.Timestamp;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

@Slf4j
public class SchedulerQuartz {

  public void start() {
    log.info("Start Scheduler");

    try {
      Scheduler scheduler = getDefaultScheduler();
      scheduler.start();

      int hour = now().getHour() == 23 ? 23 : now().getHour() + 1;

      // schedule to send email
      JobDetail jobDetailEmail = getJobDetailEmail();
      Date startAtEmail =
          Timestamp.valueOf(of(now().getYear(), now().getMonth(), now().getDayOfMonth(), hour, 5));
      Trigger triggerEmail = getTrigger("Trigger_Email", jobDetailEmail, startAtEmail);
      scheduler.scheduleJob(jobDetailEmail, triggerEmail);

      // schedule to insert/update data
      // if running on cloud (GCP/AWS), this can't be done
      if (isNotCloudDeployment()) {
        JobDetail jobDetailJsoup = getJobDetailJsoup();
        Date startAtJsoup =
            Timestamp.valueOf(
                of(now().getYear(), now().getMonth(), now().getDayOfMonth(), hour, 3));
        Trigger triggerJsoup = getTrigger("Trigger_Jsoup", jobDetailJsoup, startAtJsoup);
        scheduler.scheduleJob(jobDetailJsoup, triggerJsoup);
      }
    } catch (SchedulerException ex) {
      log.error("Start Scheduler Error", ex);
      sendEmail("Start Scheduler Error!!!");
    }

    log.info("Finish Scheduler");
  }

  private JobDetail getJobDetailJsoup() {
    return newJob(SchedulerJobJsoup.class).withIdentity("Job_Jsoup").build();
  }

  private JobDetail getJobDetailEmail() {
    return newJob(SchedulerJobEmail.class).withIdentity("Job_Email").build();
  }

  private Trigger getTrigger(String triggerIdentity, JobDetail jobDetail, Date triggerStartTime) {
    return newTrigger()
        .withIdentity(triggerIdentity)
        .startAt(triggerStartTime)
        .withSchedule(simpleSchedule().withIntervalInHours(1).repeatForever())
        .forJob(jobDetail)
        .build();
  }
}
