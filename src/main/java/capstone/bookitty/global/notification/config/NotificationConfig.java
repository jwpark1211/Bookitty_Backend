package capstone.bookitty.global.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NotificationConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}