package net.sudot.quartzschedulemanage.controller;

import lombok.extern.slf4j.Slf4j;
import net.sudot.quartzschedulemanage.config.QuartzScheduleService;
import net.sudot.quartzschedulemanage.model.JobConfig;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理器
 *
 * @author tangjialin on 2019-08-02.
 */
@Slf4j
@RestController
@RequestMapping("/jobs")
public class QuartzScheduleManageController {
    @Resource
    private QuartzScheduleService quartzScheduleService;
    @Resource
    private Scheduler scheduler;

    /**
     * 任务列表
     *
     * @return 返回所有任务
     */
    @GetMapping
    public List<JobConfig> list() throws SchedulerException {
        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
        Map<TriggerKey, Trigger> triggers = triggerKeys.stream().map(v -> {
            try {
                return scheduler.getTrigger(v);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull)
                .collect(Collectors.toMap(Trigger::getKey, trigger -> trigger));
        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyJobGroup());
        List<JobDetail> jobDetails = jobKeys.stream().map(v -> {
            try {
                return scheduler.getJobDetail(v);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());

        List<JobConfig> jobConfigs = new ArrayList<>(jobDetails.size());

        for (JobDetail jobDetail : jobDetails) {
            TriggerKey triggerKey = quartzScheduleService.buildTriggerKey(jobDetail.getKey());
            CronTrigger trigger = (CronTrigger) triggers.get(triggerKey);
            jobConfigs.add(new JobConfig()
                    .setClassName(jobDetail.getKey().getName())
                    .setCron(trigger.getCronExpression())
                    .setState(scheduler.getTriggerState(triggerKey))
                    .setDescription(jobDetail.getDescription())
                    .setStartTime(trigger.getStartTime())
                    .setPreviousFireTime(trigger.getPreviousFireTime())
                    .setNextFireTime(trigger.getNextFireTime())
            );
        }
        return jobConfigs;
    }

    @PostMapping
    public void update(@RequestBody @Validated(value = JobConfig.UpdateValid.class) JobConfig jobConfig) {
        CronScheduleBuilder scheduleBuilder = quartzScheduleService.checkAndBuildCronExpression(jobConfig.getCron());
        JobDetail jobDetail = null;
        CronTrigger trigger = null;
        try {
            jobDetail = scheduler.getJobDetail(quartzScheduleService.buildJobKey(jobConfig.getClassName()));
            trigger = (CronTrigger) scheduler.getTrigger(quartzScheduleService.buildTriggerKey(jobDetail.getKey()));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        Assert.notNull(jobDetail, "任务不存在");
        Assert.notNull(trigger, "任务不存在");
        try {
            trigger = trigger.getTriggerBuilder()
                    .withSchedule(scheduleBuilder)
                    .build();
            scheduler.rescheduleJob(trigger.getKey(), trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用此接口后,指定的任务会立即执行一次
     *
     * @param jobConfig
     */
    @PutMapping("/trigger")
    public void trigger(@RequestBody @Validated(value = JobConfig.StateChangeValid.class) JobConfig jobConfig) {
        JobKey jobKey = quartzScheduleService.buildJobKey(jobConfig.getClassName());
        try {
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            log.warn("任务触发失败", e);
        }
    }

    @PutMapping("/activate")
    public void activate(@RequestBody @Validated(value = JobConfig.StateChangeValid.class) JobConfig jobConfig) {
        JobKey jobKey = quartzScheduleService.buildJobKey(jobConfig.getClassName());
        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            log.warn("任务激活失败", e);
        }
    }

    @PutMapping("/disable")
    public void disable(@RequestBody @Validated(value = JobConfig.StateChangeValid.class) JobConfig jobConfig) {
        JobKey jobKey = quartzScheduleService.buildJobKey(jobConfig.getClassName());
        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            log.warn("任务暂停失败", e);
        }
    }

    @DeleteMapping("/delete")
    public void delete(@RequestBody @Validated(value = JobConfig.StateChangeValid.class) JobConfig jobConfig) {
        JobKey jobKey = quartzScheduleService.buildJobKey(jobConfig.getClassName());
        try {
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            log.warn("任务删除失败", e);
        }
    }
}
