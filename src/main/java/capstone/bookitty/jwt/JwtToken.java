package capstone.bookitty.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public record JwtToken (
    String grantType,
    String accessToken,
    String refreshToken
){
    public static JwtToken of(String grantType, String accessToken, String refreshToken){
        return new JwtToken(grantType,accessToken,refreshToken);
    }
}
