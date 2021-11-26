package router.usage.statistics.java.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static router.usage.statistics.java.service.Service.checkPreviousData;

public class SchedulerJobEmail implements Job {

    private static final Logger log = LoggerFactory.getLogger(SchedulerJobEmail.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Start Check Previous Data");
        checkPreviousData();
        log.info("Finish Check Previous Data");
    }
}
