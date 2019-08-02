package net.sudot.quartzschedulemanage.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * 打印
 *
 * @author tangjialin on 2019-08-01.
 */
@Component
@Slf4j
public class PrintHelloQuartzJob implements Job {
    private static int COUNT = 0;
    private int count = 0;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Hello : {} {}", ++count, ++COUNT);
    }
}
