package net.sudot.quartzschedulemanage;

import net.sudot.quartzschedulemanage.dao.JobConfigRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
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
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        String port = context.getEnvironment().getProperty("local.server.port");
        try {
            Process exec = Runtime.getRuntime().exec("cmd /c start http://localhost:" + port);
            Thread.sleep(10000);
            exec.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
