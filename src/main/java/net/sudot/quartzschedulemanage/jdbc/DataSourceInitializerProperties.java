package net.sudot.quartzschedulemanage.jdbc;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Initialize a {@link DataSourceInitializer} based on a matching {@link DataSourceInitializerProperties}
 * config.
 *
 * @author tangjialin on 2019-04-15.
 */
@ConfigurationProperties(prefix = "datasource.initializer")
public class DataSourceInitializerProperties {

    private List<Present> creates;
    private List<Present> updates;

    public List<Present> getCreates() {
        return creates;
    }

    public DataSourceInitializerProperties setCreates(List<Present> creates) {
        this.creates = creates;
        return this;
    }

    public List<Present> getUpdates() {
        return updates;
    }

    public DataSourceInitializerProperties setUpdates(List<Present> updates) {
        this.updates = updates;
        return this;
    }

    /**
     * 存在检测属性
     *
     * @author tangjialin on 2019-07-07.
     */
    public static class Present {
        private String tableName;
        private String columnName;
        private String schema;

        public String getTableName() {
            return tableName;
        }

        public Present setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public String getColumnName() {
            return columnName;
        }

        public Present setColumnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        public String getSchema() {
            return schema;
        }

        public Present setSchema(String schema) {
            this.schema = schema;
            return this;
        }
    }
}
