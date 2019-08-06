package net.sudot.quartzschedulemanage;

import net.sudot.quartzschedulemanage.dao.JobConfigRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

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
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        String port = context.getEnvironment().getProperty("local.server.port");
        try {
            Runtime.getRuntime().exec("cmd /c start http://localhost:" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
