package router.usage.statistics.java.scheduler;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Date;

import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.of;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.StdSchedulerFactory.getDefaultScheduler;
import static router.usage.statistics.java.util.Util.ACTIVE_PROFILE;
import static router.usage.statistics.java.util.Util.getSystemEnvProperty;

public class SchedulerQuartz {

    private static final Logger log = LoggerFactory.getLogger(SchedulerQuartz.class);

    public void start() {
        log.info("Start Scheduler");

        try {
            Scheduler scheduler = getDefaultScheduler();
            scheduler.start();

            // schedule to send email
            JobDetail jobDetailEmail = getJobDetailEmail();
            Date startAtEmail = Timestamp.valueOf(of(now().getYear(), now().getMonth(), now().getDayOfMonth(), now().getHour(), 5));
            Trigger triggerEmail = getTrigger("Trigger_Email", jobDetailEmail, startAtEmail);
            scheduler.scheduleJob(jobDetailEmail, triggerEmail);

            // schedule to insert/update data
            // if running on cloud (GCP/AWS), this can't be done
            String activeProfile = getSystemEnvProperty(ACTIVE_PROFILE);
            log.info("Active Profile: {}", activeProfile);

            if (!"cloud".equalsIgnoreCase(activeProfile)) {
                JobDetail jobDetailJsoup = getJobDetailJsoup();
                Date startAtJsoup = Timestamp.valueOf(of(now().getYear(), now().getMonth(), now().getDayOfMonth(), now().getHour(), 3));
                Trigger triggerJsoup = getTrigger("Trigger_Jsoup", jobDetailJsoup, startAtJsoup);
                scheduler.scheduleJob(jobDetailJsoup, triggerJsoup);
            }
        } catch (SchedulerException ex) {
            log.error("Start Scheduler Error", ex);
        }

        log.info("Finish Scheduler");
    }

    private JobDetail getJobDetailJsoup() {
        return newJob(SchedulerJobJsoup.class)
                .withIdentity("Job_Jsoup")
                .build();
    }

    private JobDetail getJobDetailEmail() {
        return newJob(SchedulerJobEmail.class)
                .withIdentity("Job_Email")
                .build();
    }

    private Trigger getTrigger(String triggerIdentity, JobDetail jobDetail, Date triggerStartTime) {
        return newTrigger()
                .withIdentity(triggerIdentity)
                .startAt(triggerStartTime)
                .withSchedule(simpleSchedule()
                        .withIntervalInHours(1)
                        .repeatForever())
                .forJob(jobDetail)
                .build();
    }
}
