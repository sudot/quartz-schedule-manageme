package net.sudot.quartzschedulemanage.controller;

import net.sudot.quartzschedulemanage.config.QuartzScheduleService;
import net.sudot.quartzschedulemanage.model.JobConfig;
import net.sudot.quartzschedulemanage.model.State;
import net.sudot.quartzschedulemanage.service.JobConfigService;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 管理器
 *
 * @author tangjialin on 2019-08-02.
 */
@RestController
@RequestMapping("/jobs")
public class QuartzScheduleManageController {
    @Resource
    private JobConfigService jobConfigService;
    @Resource
    private QuartzScheduleService quartzScheduleService;

    /**
     * 任务列表
     *
     * @return 返回所有任务
     */
    @GetMapping
    public Iterable<JobConfig> list() {
        return jobConfigService.findAll();
    }

    @PostMapping
    public void save(@RequestBody @Valid JobConfig jobConfig) {
        quartzScheduleService.checkCronExpression(jobConfig.getCron());
        jobConfigService.save(jobConfig);
        quartzScheduleService.addJob(jobConfig);
    }

    @PutMapping("/{id}/activate")
    public void activate(@PathVariable Long id) {
        JobConfig jobConfig = jobConfigService.findById(id);
        Assert.notNull(jobConfig, "数据不存在");
        Assert.isTrue(jobConfig.getState() != State.ACTIVATE, "任务已激活,无需重复操作");
        jobConfigService.save(jobConfig.setState(State.ACTIVATE));
        quartzScheduleService.addJob(jobConfig);
    }

    @PutMapping("/{id}/disable")
    public void disable(@PathVariable Long id) {
        JobConfig jobConfig = jobConfigService.findById(id);
        Assert.notNull(jobConfig, "数据不存在");
        Assert.isTrue(jobConfig.getState() != State.DISABLED, "任务已禁用,无需重复操作");
        jobConfigService.save(jobConfig.setState(State.DISABLED));
        quartzScheduleService.deleteJob(jobConfig);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        JobConfig jobConfig = jobConfigService.findById(id);
        Assert.notNull(jobConfig, "数据不存在");
        jobConfigService.delete(jobConfig);
        quartzScheduleService.deleteJob(jobConfig);
    }
}
