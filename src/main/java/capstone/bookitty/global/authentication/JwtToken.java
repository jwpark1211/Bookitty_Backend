package capstone.bookitty.global.authentication;

public record JwtToken (
    String grantType,
    String accessToken,
    String refreshToken
){
    public static JwtToken of(String grantType, String accessToken, String refreshToken){
        return new JwtToken(grantType,accessToken,refreshToken);
    }
}
