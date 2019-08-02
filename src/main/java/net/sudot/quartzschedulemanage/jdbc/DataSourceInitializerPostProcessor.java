package net.sudot.quartzschedulemanage.jdbc;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import javax.sql.DataSource;

/**
 * @author tangjialin on 2019-07-23.
 */
@Configuration
@Import({DataSourceInitializer.class, DataSourceInitializerProperties.class})
public class DataSourceInitializerPostProcessor implements BeanPostProcessor, Ordered {

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource) {
            // force initialization of this bean as soon as we see a DataSource
            DataSourceInitializer initializer = this.beanFactory.getBean(DataSourceInitializer.class);
            initializer.createSchema();
            initializer.updateSchema();
        }
        return bean;
    }

}