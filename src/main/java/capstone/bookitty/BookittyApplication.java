package capstone.bookitty;

import capstone.bookitty.global.config.MetaDBConfig;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableBatchProcessing(
		dataSourceRef = MetaDBConfig.META_DATASOURCE,
		transactionManagerRef = MetaDBConfig.META_TRANSACTION_MANAGER
)
public class BookittyApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookittyApplication.class, args);
	}

}
