package net.sudot.quartzschedulemanage.controller;

import lombok.extern.slf4j.Slf4j;
import net.sudot.quartzschedulemanage.model.JobConfig;
import net.sudot.quartzschedulemanage.service.QuartzScheduleManageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

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
    private QuartzScheduleManageService quartzScheduleManageService;

    /**
     * 任务列表
     *
     * @return 返回所有任务
     */
    @GetMapping
    public List<JobConfig> list() {
        return quartzScheduleManageService.findAll();
    }

    @PostMapping
    public void update(@RequestBody @Validated(value = JobConfig.UpdateValid.class) JobConfig jobConfig) {
        quartzScheduleManageService.update(jobConfig.getClassName(), jobConfig.getCron());
    }

    /**
     * 调用此接口后,指定的任务会立即执行一次
     *
     * @param jobConfig
     */
    @PutMapping("/trigger")
    public void trigger(@RequestBody @Validated(value = JobConfig.StateChangeValid.class) JobConfig jobConfig) {
        quartzScheduleManageService.trigger(jobConfig.getClassName());
    }

    @PutMapping("/activate")
    public void activate(@RequestBody @Validated(value = JobConfig.StateChangeValid.class) JobConfig jobConfig) {
        quartzScheduleManageService.activate(jobConfig.getClassName());
    }

    @PutMapping("/disable")
    public void disable(@RequestBody @Validated(value = JobConfig.StateChangeValid.class) JobConfig jobConfig) {
        quartzScheduleManageService.disable(jobConfig.getClassName());
    }

    @DeleteMapping("/delete")
    public void delete(@RequestBody @Validated(value = JobConfig.StateChangeValid.class) JobConfig jobConfig) {
        quartzScheduleManageService.delete(jobConfig.getClassName());
    }
}
