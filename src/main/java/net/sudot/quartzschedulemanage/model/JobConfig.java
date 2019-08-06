package net.sudot.quartzschedulemanage.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.quartz.Trigger;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

/**
 * 作业配置
 *
 * @author tangjialin on 2019-08-02.
 */
@Data
@Accessors(chain = true)
public class JobConfig implements Serializable {
    /** 任务执行类,必须唯一 */
    @NotEmpty(message = "任务执行类不能为空", groups = StateChangeValid.class)
    private String className;
    /** cron表达式 */
    @NotEmpty(message = "任务执行表达式不能为空", groups = UpdateValid.class)
    private String cron;
    /** 任务状态 */
    private Trigger.TriggerState state;
    /** 任务说明 */
    private String description;
    /** 触发器从创建开始第一次触发的时间 */
    private Date startTime;
    /** 上次触发时间 */
    private Date previousFireTime;
    /** 下次触发时间 */
    private Date nextFireTime;

    /**
     * 状态变更验证
     *
     * @author tangjialin on 2019-08-06.
     */
    public interface StateChangeValid {
    }

    /**
     * 更新计划信息验证,包含状态变更验证项
     *
     * @author tangjialin on 2019-08-06.
     */
    public interface UpdateValid extends StateChangeValid {
    }

}
