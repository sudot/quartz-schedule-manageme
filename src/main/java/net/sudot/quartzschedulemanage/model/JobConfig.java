package net.sudot.quartzschedulemanage.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 作业配置
 *
 * @author tangjialin on 2019-08-02.
 */
@Data
@Accessors(chain = true)
public class JobConfig implements Serializable {
    @Id
    private Long id;
    /** 编号,必须唯一 */
    @NotEmpty(message = "编号不能为空")
    private String key;
    /** 任务名称 */
    @NotEmpty(message = "任务名称不能为空")
    private String name;
    /** 任务状态 */
    private State state;
    /** cron表达式 */
    @NotEmpty(message = "任务执行表达式不能为空")
    private String cron;
    /** 任务执行类名称 */
    @NotEmpty(message = "任务执行类名称不能为空")
    private String className;
}
