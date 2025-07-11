package capstone.bookitty.domain.member.api.dto.tokenDto;

public record TokenRequest(
        String accessToken,
        String refreshToken
) {
}
