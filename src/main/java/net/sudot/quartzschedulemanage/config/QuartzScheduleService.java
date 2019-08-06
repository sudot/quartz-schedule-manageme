package net.sudot.quartzschedulemanage.config;

import net.sudot.quartzschedulemanage.config.annotation.JobDefinition;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.reflections.Reflections;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * QuartzSchedule初始化
 *
 * @author tangjialin on 2019-08-02.
 */
@Configuration
public class QuartzScheduleService implements InitializingBean {
    @Resource
    private Scheduler scheduler;
    private Map<String, Class<? extends Job>> jobClassNameMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        Reflections reflections = new Reflections("net.sudot.quartzschedulemanage.job");
        Set<Class<? extends Job>> subTypes = reflections.getSubTypesOf(Job.class);
        for (Class<? extends Job> jobClass : subTypes) {
            jobClassNameMap.put(jobClass.getName(), jobClass);
            JobDefinition jobDefinition = jobClass.getDeclaredAnnotation(JobDefinition.class);
            try {
                JobKey jobKey = buildJobKey(jobClass);
                TriggerKey triggerKey = buildTriggerKey(jobKey);
                CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
                if (trigger == null) {
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    if (jobDetail == null) {
                        jobDetail = JobBuilder.newJob(jobClass)
                                .withIdentity(jobKey)
                                .withDescription(jobDefinition.description())
                                .build();
                    }

                    trigger = TriggerBuilder.newTrigger()
                            .forJob(jobDetail)
                            .withIdentity(triggerKey)
                            .withSchedule(buildCronExpression(jobDefinition.cron()))
                            .withDescription(jobDefinition.description())
                            .build();
                    scheduler.scheduleJob(jobDetail, trigger);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JobKey buildJobKey(String className) {
        Class<? extends Job> jobClass = jobClassNameMap.get(className);
        Assert.notNull(jobClass, "未找到[" + className + "]对应的任务");
        return buildJobKey(jobClass);
    }

    public JobKey buildJobKey(Class<? extends Job> jobClass) {
        return JobKey.jobKey(jobClass.getName());
    }

    public TriggerKey buildTriggerKey(JobKey jobKey) {
        return TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup());
    }

    public CronScheduleBuilder buildCronExpression(String cronExpression) {
        return CronScheduleBuilder.cronSchedule(cronExpression);
    }

    public CronScheduleBuilder checkAndBuildCronExpression(String cronExpression) {
        try {
            return buildCronExpression(cronExpression);
        } catch (Exception e) {
            throw new IllegalArgumentException("cron表达式错误:[" + cronExpression + "]");
        }
    }
}
