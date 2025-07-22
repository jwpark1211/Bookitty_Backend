package capstone.bookitty.global.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@Configuration
@EnableBatchProcessing(
        dataSourceRef = MetaDBConfig.META_DATASOURCE,
        transactionManagerRef = MetaDBConfig.META_TRANSACTION_MANAGER
)
public class BatchInfrastructureConfig {
}
