package net.sudot.quartzschedulemanage.job;

import lombok.extern.slf4j.Slf4j;
import net.sudot.quartzschedulemanage.config.annotation.JobDefinition;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 打印
 *
 * @author tangjialin on 2019-08-01.
 */
@Slf4j
@JobDefinition(cron = "0/5 * * * * ?", description = "打印Word")
public class PrintWordQuartzJob implements Job {
    private static int COUNT = 0;
    private int count = 0;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Word : {} {}", ++count, ++COUNT);
    }
}
