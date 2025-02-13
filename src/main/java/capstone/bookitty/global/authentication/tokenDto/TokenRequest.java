package capstone.bookitty.global.authentication.tokenDto;

public record TokenRequest(
    String accessToken,
    String refreshToken
){ }
