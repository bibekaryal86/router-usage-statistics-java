package router.usage.statistics.java.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static router.usage.statistics.java.service.Service.insertDataUsages;

public class SchedulerJobJsoup implements Job {

    private static final Logger log = LoggerFactory.getLogger(SchedulerJobJsoup.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Start Insert Data Usages Job");
        insertDataUsages();
        log.info("Finish Insert Data Usages Job");
    }
}
