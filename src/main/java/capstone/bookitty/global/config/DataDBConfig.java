package capstone.bookitty.global.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = {
                "capstone.bookitty.domain",
                "capstone.bookitty.global.authentication"
        },
        entityManagerFactoryRef = "dataEntityManager",
        transactionManagerRef = "dataTransactionManager"
)
public class DataDBConfig {

    public static final String DATA_DATASOURCE = "dataDataSource";
    public static final String DATA_ENTITY_MANAGER = "dataEntityManager";
    public static final String DATA_TRANSACTION_MANAGER = "dataTransactionManager";

    @Primary
    @Bean(name = DATA_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.data")
    public DataSource dataDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = DATA_ENTITY_MANAGER)
    public LocalContainerEntityManagerFactoryBean dataEntityManager(
            @Qualifier(DATA_DATASOURCE) DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        em.setDataSource(dataSource);
        em.setPackagesToScan("capstone.bookitty");
        em.setPersistenceUnitName("dataEntityManager");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Primary
    @Bean(name = DATA_TRANSACTION_MANAGER)
    public PlatformTransactionManager dataTransactionManager(
            @Qualifier(DATA_ENTITY_MANAGER) LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
