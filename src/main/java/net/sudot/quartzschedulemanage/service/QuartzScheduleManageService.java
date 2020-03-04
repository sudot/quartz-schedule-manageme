package net.sudot.quartzschedulemanage.service;

import net.sudot.quartzschedulemanage.model.JobConfig;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * QuartzSchedule服务操作
 *
 * @author tangjialin on 2019-08-02.
 */
@Service
public class QuartzScheduleManageService {
    @Resource
    private Scheduler scheduler;

    /**
     * 获取所有Schedule Job信息
     *
     * @return Schedule Job信息
     */
    public List<JobConfig> findAll() {
        try {
            Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
            Map<TriggerKey, Trigger> triggers = new LinkedHashMap<>();
            for (TriggerKey triggerKey : triggerKeys) {
                Trigger trigger = scheduler.getTrigger(triggerKey);
                if (trigger == null) { continue; }
                triggers.put(triggerKey, trigger);
            }
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyJobGroup());
            List<JobConfig> jobConfigs = new ArrayList<>(jobKeys.size());
            for (JobKey jobKey : jobKeys) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                TriggerKey triggerKey = buildTriggerKey(jobDetail.getKey());
                CronTrigger trigger = (CronTrigger) triggers.get(triggerKey);
                jobConfigs.add(new JobConfig()
                        .setClassName(jobDetail.getKey().getName())
                        .setCron(trigger.getCronExpression())
                        .setState(scheduler.getTriggerState(triggerKey))
                        .setDescription(trigger.getDescription())
                        .setStartTime(trigger.getStartTime())
                        .setPreviousFireTime(trigger.getPreviousFireTime())
                        .setNextFireTime(trigger.getNextFireTime())
                );
            }
            return jobConfigs;
        } catch (SchedulerException e) {
            throw new IllegalArgumentException("Schedule Job获取失败", e);
        }
    }

    /**
     * 获取Schedule Job信息
     *
     * @param taskId 任务编号
     * @return Schedule Job信息
     */
    public JobConfig find(String taskId) {
        try {
            JobKey jobKey = buildJobKey(taskId);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail == null) { return null; }
            TriggerKey triggerKey = buildTriggerKey(jobKey);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            return new JobConfig()
                    .setClassName(jobDetail.getKey().getName())
                    .setCron(trigger.getCronExpression())
                    .setState(scheduler.getTriggerState(triggerKey))
                    .setDescription(trigger.getDescription())
                    .setStartTime(trigger.getStartTime())
                    .setPreviousFireTime(trigger.getPreviousFireTime())
                    .setNextFireTime(trigger.getNextFireTime());
        } catch (SchedulerException e) {
            throw new IllegalArgumentException("Schedule Job获取失败:" + taskId, e);
        }
    }

    /**
     * 添加Schedule Job
     *
     * @param taskId   任务编号
     * @param cron     cron表达式
     * @param jobClass 触发的执行类
     */
    public void addOrUpdate(String taskId, String cron, Class<? extends Job> jobClass) {
        addOrUpdate(taskId, cron, null, jobClass);
    }

    /**
     * 添加Schedule Job
     *
     * @param taskId      任务编号
     * @param cron        cron表达式
     * @param description 任务说明
     * @param jobClass    触发的执行类
     */
    public void addOrUpdate(String taskId, String cron, String description, Class<? extends Job> jobClass) {
        try {
            JobKey jobKey = buildJobKey(taskId);
            TriggerKey triggerKey = buildTriggerKey(jobKey);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                if (jobDetail == null) {
                    jobDetail = JobBuilder.newJob(jobClass)
                            .withIdentity(jobKey)
                            .withDescription(description)
                            .build();
                }

                trigger = TriggerBuilder.newTrigger()
                        .forJob(jobDetail)
                        .withIdentity(triggerKey)
                        .withSchedule(checkAndBuildCronExpression(cron))
                        .withDescription(description)
                        .build();
                scheduler.scheduleJob(jobDetail, trigger);
            } else {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                trigger = TriggerBuilder.newTrigger()
                        .forJob(jobDetail)
                        .withIdentity(triggerKey)
                        .withSchedule(checkAndBuildCronExpression(cron))
                        .withDescription(description)
                        .build();
                scheduler.rescheduleJob(trigger.getKey(), trigger);
            }
        } catch (SchedulerException e) {
            throw new IllegalArgumentException("Scheduler任务添加失败:" + taskId + ":" + cron, e);
        }
    }

    /**
     * 更新Schedule Job
     *
     * @param taskId 任务编号
     * @param cron   cron表达式
     */
    public void update(String taskId, String cron) {
        CronScheduleBuilder scheduleBuilder = checkAndBuildCronExpression(cron);
        try {
            JobDetail jobDetail = scheduler.getJobDetail(buildJobKey(taskId));
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(buildTriggerKey(jobDetail.getKey()));
            Assert.notNull(jobDetail, "任务不存在");
            Assert.notNull(trigger, "任务不存在");
            trigger = trigger.getTriggerBuilder()
                    .withSchedule(scheduleBuilder)
                    .build();
            scheduler.rescheduleJob(trigger.getKey(), trigger);
        } catch (SchedulerException e) {
            throw new IllegalArgumentException("Scheduler任务更新失败", e);
        }
    }

    /**
     * 立刻触发Schedule Job
     *
     * @param taskId 任务编号
     */
    public void trigger(String taskId) {
        JobKey jobKey = buildJobKey(taskId);
        try {
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            throw new IllegalArgumentException("Scheduler任务触发失败:" + taskId, e);
        }
    }

    /**
     * 激活指定Schedule Job
     *
     * @param taskId 任务编号
     */
    public void activate(String taskId) {
        JobKey jobKey = buildJobKey(taskId);
        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            throw new IllegalArgumentException("Scheduler任务激活失败:" + taskId, e);
        }
    }

    /**
     * 禁用(暂停)指定Schedule Job
     *
     * @param taskId 任务编号
     */
    public void disable(String taskId) {
        JobKey jobKey = buildJobKey(taskId);
        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            throw new IllegalArgumentException("Scheduler任务暂停失败:" + taskId, e);
        }
    }

    /**
     * 删除指定Schedule Job
     *
     * @param taskId 任务编号
     */
    public void delete(String taskId) {
        JobKey jobKey = buildJobKey(taskId);
        try {
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            throw new IllegalArgumentException("Scheduler任务删除失败:" + taskId, e);
        }
    }

    /**
     * 删除所有的Schedule Job
     */
    public void deleteAll() {
        try {
            scheduler.clear();
        } catch (SchedulerException e) {
            throw new IllegalArgumentException("删除所有：Scheduler任务删除失败", e);
        }
    }

    /**
     * 创建一个JobKey
     *
     * @param taskId 任务编号
     * @return 返回JobKey
     */
    public JobKey buildJobKey(String taskId) {
        if (taskId != null) {
            taskId = taskId.trim();
        }
        return JobKey.jobKey(taskId);
    }

    /**
     * 创建一个TriggerKey
     *
     * @param jobKey JobKey
     * @return 返回TriggerKey
     */
    public TriggerKey buildTriggerKey(JobKey jobKey) {
        return TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup());
    }

    /**
     * 创建一个cron表达式计划生成器
     *
     * @param cronExpression cron表达式
     * @return 返回cron表达式计划生成器
     * @throws ParseException 若表达式错误,则抛出此异常
     */
    public CronScheduleBuilder buildCronExpression(String cronExpression) throws ParseException {
        if (cronExpression != null) {
            cronExpression = cronExpression.trim();
        }
        return CronScheduleBuilder.cronSchedule(new CronExpression(cronExpression));
    }

    /**
     * 检查并创建一个cron表达式计划生成器
     *
     * @param cronExpression cron表达式
     * @return 返回cron表达式计划生成器
     * @throws IllegalArgumentException 若表达式错误,则抛出此异常
     */
    public CronScheduleBuilder checkAndBuildCronExpression(String cronExpression) {
        try {
            return buildCronExpression(cronExpression);
        } catch (Exception e) {
            throw new IllegalArgumentException("cron表达式错误:[" + cronExpression + "]");
        }
    }
}
