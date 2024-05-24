package capstone.bookitty.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secretKey;
    private String authType;
    private String authoritiesKey;
    private String accessTokenHeader;
    private String refreshTokenHeader;
    private Long accessTokenExpire;
    private Long refreshTokenExpire;
}
