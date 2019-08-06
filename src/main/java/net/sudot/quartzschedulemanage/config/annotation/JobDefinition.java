package net.sudot.quartzschedulemanage.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 任务信息定义
 *
 * @author tangjialin on 2019-08-05.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JobDefinition {
    /** 任务描述 */
    String description();

    /** cron表达式 */
    String cron();
}
