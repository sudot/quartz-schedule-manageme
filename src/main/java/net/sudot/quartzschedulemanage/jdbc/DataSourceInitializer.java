package net.sudot.quartzschedulemanage.jdbc;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.config.SortedResourcesFactoryBean;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Initialize a {@link DataSource} based on a matching {@link DataSourceProperties}
 * config.
 *
 * @author tangjialin on 2019-04-15.
 */
class DataSourceInitializer {

    private static final String[] JDBC_METADATA_TABLE_TYPES = {"TABLE"};
    private final DataSource dataSource;
    private final DataSourceProperties properties;
    private final ResourceLoader resourceLoader;
    private final DataSourceInitializerProperties initializerProperties;
    private String databaseType;

    /**
     * Create a new instance with the {@link DataSource} to initialize and its matching
     * {@link DataSourceProperties configuration}.
     *
     * @param dataSource            the datasource to initialize
     * @param properties            the matching configuration
     * @param resourceLoader        the resource loader to use (can be null)
     * @param initializerProperties 初始化信息
     */
    DataSourceInitializer(DataSource dataSource, DataSourceProperties properties,
                          ResourceLoader resourceLoader, DataSourceInitializerProperties initializerProperties) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
        this.initializerProperties = (initializerProperties != null ? initializerProperties : new DataSourceInitializerProperties());
    }

    /**
     * org.activiti.engine.impl.db.DbSqlSession#isTablePresent
     * Create the schema if necessary.
     *
     * @return {@code true} if the schema was created
     * @see DataSourceProperties#getSchema()
     */
    public boolean createSchema() {
        boolean isRunScript = false;
        List<DataSourceInitializerProperties.Present> presents = Optional.ofNullable(initializerProperties.getCreates())
                .orElseGet(ArrayList::new);
        for (DataSourceInitializerProperties.Present present : presents) {
            String tableName = present.getTableName();
            if (isTablePresent(tableName)) { continue; }
            List<Resource> scripts = getScripts("create", present.getSchema());
            if (scripts.isEmpty()) { return false; }
            String username = this.properties.getSchemaUsername();
            String password = this.properties.getSchemaPassword();
            runScripts(scripts, username, password);
            isRunScript = true;
        }
        return isRunScript;
    }

    /**
     * update the schema if necessary.
     *
     * @see DataSourceProperties#getData()
     */
    public void updateSchema() {
        List<DataSourceInitializerProperties.Present> presents = Optional.ofNullable(initializerProperties.getUpdates())
                .orElseGet(ArrayList::new);
        for (DataSourceInitializerProperties.Present present : presents) {
            String tableName = present.getTableName();
            if (isTableColumnPresent(tableName, present.getColumnName())) { continue; }
            List<Resource> scripts = getScripts("upgrade", present.getSchema());
            if (scripts.isEmpty()) { continue; }
            String username = this.properties.getSchemaUsername();
            String password = this.properties.getSchemaPassword();
            runScripts(scripts, username, password);
        }
    }

    private List<Resource> getScripts(String operation, String schema) {
        List<String> fallbackResources = new ArrayList<>();
        fallbackResources.add(String.format("classpath*:db/%s/%s.%s.sql", operation, databaseType, schema));
        List<Resource> resources = new ArrayList<>();
        for (String location : fallbackResources) {
            for (Resource resource : doGetResources(location)) {
                if (!resource.exists()) { continue; }
                resources.add(resource);
            }
        }
        return resources;
    }

    private Resource[] doGetResources(String location) {
        try {
            SortedResourcesFactoryBean factory = new SortedResourcesFactoryBean(
                    this.resourceLoader, Collections.singletonList(location));
            factory.afterPropertiesSet();
            return factory.getObject();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load resources from " + location, ex);
        }
    }

    private void runScripts(List<Resource> resources, String username, String password) {
        if (resources.isEmpty()) {
            return;
        }
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(this.properties.isContinueOnError());
        populator.setSeparator(this.properties.getSeparator());
        if (this.properties.getSqlScriptEncoding() != null) {
            populator.setSqlScriptEncoding(this.properties.getSqlScriptEncoding().name());
        }
        for (Resource resource : resources) {
            populator.addScript(resource);
        }
        DataSource dataSource = this.dataSource;
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            dataSource = DataSourceBuilder.create(this.properties.getClassLoader())
                    .driverClassName(this.properties.determineDriverClassName())
                    .url(this.properties.determineUrl()).username(username)
                    .password(password).build();
        }
        DatabasePopulatorUtils.execute(populator, dataSource);
    }

    public boolean isTablePresent(String tableName) {
        Connection connection = null;
        try {
            connection = this.dataSource.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();

            databaseType = databaseMetaData.getDatabaseProductName().toLowerCase();
            databaseType = "Microsoft SQL Server".equalsIgnoreCase(databaseType) ? "mssql" : databaseType;
            if ("postgres".equals(databaseType)) {
                tableName = tableName.toLowerCase();
            } else {
                tableName = tableName.toUpperCase();
            }

            try (ResultSet tables = databaseMetaData.getTables(null, null, tableName, JDBC_METADATA_TABLE_TYPES)) {
                return tables.next();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("couldn't check if tables are already present using metadata: " + e.getMessage(), e);
        }
    }

    public boolean isTableColumnPresent(String tableName, String columnName) {
        Connection connection = null;
        try {
            connection = this.dataSource.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            if ("postgres".equals(databaseType)) {
                tableName = tableName.toLowerCase();
                columnName = columnName.toLowerCase();
            } else {
                tableName = tableName.toUpperCase();
                columnName = columnName.toUpperCase();
            }
            try (ResultSet tables = databaseMetaData.getColumns(null, null, tableName, columnName)) {
                return tables.next();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("couldn't check if tables are already present using metadata: " + e.getMessage(), e);
        }
    }
}
