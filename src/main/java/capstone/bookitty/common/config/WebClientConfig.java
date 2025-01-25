package capstone.bookitty.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    @Value("${api.aladin.uri}")
    private String aladinUri;

    @Bean @Qualifier("AladinWebClient")
    public WebClient aladinWebClientApi(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .clone()
                .baseUrl(aladinUri)
                .build();
    }

}
