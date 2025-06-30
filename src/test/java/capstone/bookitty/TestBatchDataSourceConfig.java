package capstone.bookitty;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@TestConfiguration
public class TestBatchDataSourceConfig {
    @Bean(name = "metaDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource metaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "metaTransactionManager")
    public PlatformTransactionManager metaTransactionManager(
            @Qualifier("metaDataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }
}