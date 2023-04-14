package router.usage.statistics.java.scheduler;

import static router.usage.statistics.java.service.Service.insertDataUsages;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
public class SchedulerJobJsoup implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("Start Insert Data Usages Job");
    insertDataUsages();
    log.info("Finish Insert Data Usages Job");
  }
}
