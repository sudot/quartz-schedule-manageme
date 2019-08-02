package net.sudot.quartzschedulemanage;

import net.sudot.quartzschedulemanage.dao.JobConfigRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 程序入口
 *
 * @author tangjialin on 2019-08-01.
 */
@EnableScheduling
@EnableJdbcRepositories(basePackageClasses = JobConfigRepository.class)
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
