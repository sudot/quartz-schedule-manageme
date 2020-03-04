package net.sudot.quartzschedulemanage.config;

import net.sudot.quartzschedulemanage.config.annotation.JobDefinition;
import net.sudot.quartzschedulemanage.service.QuartzScheduleManageService;
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
public class QuartzScheduleInitializing implements InitializingBean {
    @Resource
    private Scheduler scheduler;
    @Resource
    private QuartzScheduleManageService quartzScheduleManageService;

    private Map<String, Class<? extends Job>> jobClassNameMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        Reflections reflections = new Reflections("net.sudot.quartzschedulemanage.job");
        Set<Class<? extends Job>> subTypes = reflections.getSubTypesOf(Job.class);
        for (Class<? extends Job> jobClass : subTypes) {
            jobClassNameMap.put(jobClass.getName(), jobClass);
            JobDefinition jobDefinition = jobClass.getDeclaredAnnotation(JobDefinition.class);
            try {
                JobKey jobKey = quartzScheduleManageService.buildJobKey(jobClass.getName());
                TriggerKey triggerKey = quartzScheduleManageService.buildTriggerKey(jobKey);
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
                            .withSchedule(quartzScheduleManageService.buildCronExpression(jobDefinition.cron()))
                            .withDescription(jobDefinition.description())
                            .build();
                    scheduler.scheduleJob(jobDetail, trigger);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
