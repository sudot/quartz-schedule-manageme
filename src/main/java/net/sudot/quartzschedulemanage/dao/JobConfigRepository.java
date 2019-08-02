package net.sudot.quartzschedulemanage.dao;

import net.sudot.quartzschedulemanage.model.JobConfig;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * 任务配置
 *
 * @author tangjialin on 2019-08-02.
 */
public interface JobConfigRepository extends CrudRepository<JobConfig, Long> {
    /**
     * 按Key查询实体
     *
     * @param key key
     * @return 返回查询的实体
     */
    @Query("select * from job_config where key = :key")
    JobConfig findByKey(String key);
}
