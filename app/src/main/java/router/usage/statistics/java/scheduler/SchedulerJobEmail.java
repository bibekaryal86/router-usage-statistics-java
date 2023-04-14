package router.usage.statistics.java.scheduler;

import static router.usage.statistics.java.service.Service.checkPreviousData;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
public class SchedulerJobEmail implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("Start Check Previous Data for Email Job");
    checkPreviousData();
    log.info("Finish Check Previous Data for Email Job");
  }
}
