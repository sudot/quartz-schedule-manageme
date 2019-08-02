package net.sudot.quartzschedulemanage.service;

import net.sudot.quartzschedulemanage.dao.JobConfigRepository;
import net.sudot.quartzschedulemanage.model.JobConfig;
import net.sudot.quartzschedulemanage.model.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 任务配置服务
 *
 * @author tangjialin on 2019-08-02.
 */
@Service
public class JobConfigService {
    @Autowired
    private JobConfigRepository jobConfigRepository;

    /**
     * 获得所有实体
     *
     * @return 返回获得的实体
     */
    public Iterable<JobConfig> findAll() {
        return jobConfigRepository.findAll();
    }

    /**
     * 通过主键获得实体
     *
     * @param id 主键
     * @return 返回获得的实体, 若实体不存在返回null
     */
    public JobConfig findById(Long id) {
        return jobConfigRepository.findById(id).orElse(null);
    }

    /**
     * 新增和修改实体
     *
     * @param jobConfig 保存的实体
     * @return 返回保存后的实体
     */
    @Transactional(rollbackFor = Exception.class)
    public JobConfig save(JobConfig jobConfig) {
        JobConfig byKey = jobConfigRepository.findByKey(jobConfig.getKey());
        if (jobConfig.getId() == null) {
            Assert.isNull(byKey, "任务KEY[" + jobConfig.getKey() + "]已存在,请勿重复添加");
        } else {
            if (byKey != null && !byKey.getId().equals(jobConfig.getId())) {
                throw new IllegalArgumentException("任务KEY[" + jobConfig.getKey() + "]已存在,请勿重复添加");
            }
            Assert.isTrue(byKey == null || byKey.getId().equals(jobConfig.getId()),
                    "任务KEY[" + jobConfig.getKey() + "]已存在,请勿重复添加");
        }
        if (jobConfig.getState() == null) {
            jobConfig.setState(State.ACTIVATE);
        }
        return jobConfigRepository.save(jobConfig);
    }

    /**
     * 新增和修改实体
     *
     * @param jobConfig 保存的实体
     * @return 返回保存后的实体
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(JobConfig jobConfig) {
        jobConfigRepository.deleteById(jobConfig.getId());
    }
}
