package net.sudot.quartzschedulemanage.config;

import net.sudot.quartzschedulemanage.model.JobConfig;
import net.sudot.quartzschedulemanage.model.State;
import net.sudot.quartzschedulemanage.service.JobConfigService;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * QuartzSchedule初始化
 *
 * @author tangjialin on 2019-08-02.
 */
@Configuration
public class QuartzScheduleService implements InitializingBean {
    @Resource
    private Scheduler scheduler;
    @Resource
    private JobConfigService jobConfigService;

    @Override
    public void afterPropertiesSet() throws Exception {
        Iterable<JobConfig> jobConfigs = jobConfigService.findAll();
        jobConfigs.forEach(jobConfig -> {
            if (!State.ACTIVATE.equals(jobConfig.getState())) { return; }
            addJob(jobConfig);
        });
    }

    @SuppressWarnings("unchecked")
    public void addJob(JobConfig jobConfig) {
        // 表达式调度构建器
        CronScheduleBuilder scheduleBuilder = builderCronExpression(jobConfig.getCron());
        Class<Job> jobClass = null;
        try {
            jobClass = (Class<Job>) Class.forName(jobConfig.getClassName());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("任务执行类名称错误。需要类全限定名称，包含包名");
        }
        JobKey jobKey = buildJobKey(jobConfig);
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup());
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                if (jobDetail == null) {
                    jobDetail = JobBuilder.newJob(jobClass)
                            .withIdentity(jobKey)
                            .withDescription(jobConfig.getName())
                            .build();
                }

                trigger = TriggerBuilder.newTrigger()
                        .forJob(jobDetail)
                        .withIdentity(triggerKey)
                        .withSchedule(scheduleBuilder)
                        .withDescription(jobConfig.getName())
                        .build();
                scheduler.scheduleJob(jobDetail, trigger);
            } else {
                trigger = trigger.getTriggerBuilder()
                        .withSchedule(scheduleBuilder)
                        .withDescription(jobConfig.getName())
                        .build();
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteJob(JobConfig jobConfig) {
        // 从任务计划中移除任务
        try {
            scheduler.deleteJob(buildJobKey(jobConfig));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public JobKey buildJobKey(JobConfig jobConfig) {
        return JobKey.jobKey(jobConfig.getKey(), jobConfig.getClassName());
    }

    public CronScheduleBuilder builderCronExpression(String cronExpression) {
        return CronScheduleBuilder.cronSchedule(cronExpression);
    }

    public void checkCronExpression(String cronExpression) {
        try {
            builderCronExpression(cronExpression);
        } catch (Exception e) {
            throw new IllegalArgumentException("cron表达式错误:[" + cronExpression + "]");
        }
    }
}
