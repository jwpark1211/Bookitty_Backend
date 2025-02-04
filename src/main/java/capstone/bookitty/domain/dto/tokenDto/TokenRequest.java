package capstone.bookitty.domain.dto.tokenDto;

public record TokenRequest(
    String accessToken,
    String refreshToken
){ }
