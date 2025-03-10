package capstone.bookitty.global.config;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class MetaDBConfig {

    public static final String META_DATASOURCE = "metaDataSource";
    public static final String META_TRANSACTION_MANAGER = "metaTransactionManager";

    @Bean(name = META_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.meta")
    public DataSource metaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = META_TRANSACTION_MANAGER)
    public PlatformTransactionManager metaTransactionManager(
            @Qualifier(META_DATASOURCE) DataSource metaDataSource
    ) {
        return new DataSourceTransactionManager(metaDataSource);
    }

    @Primary
    @Bean(name = "metaJobRepository")
    public JobRepository metaJobRepository(
            @Qualifier(META_DATASOURCE) DataSource metaDataSource,
            @Qualifier(META_TRANSACTION_MANAGER) PlatformTransactionManager metaTx
    ) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(metaDataSource);
        factory.setTransactionManager(metaTx);
        factory.setValidateTransactionState(false);
        factory.afterPropertiesSet();
        return factory.getObject();
    }


    @Primary
    @Bean(name = "metaJobLauncher")
    public JobLauncher metaJobLauncher(
            @Qualifier("metaJobRepository") JobRepository metaJobRepository
    ) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(metaJobRepository);
        launcher.afterPropertiesSet();
        return launcher;
    }

}
