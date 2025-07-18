package capstone.bookitty.domain.member.api.dto.tokenDto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank(message = "Access token is a required entry value")
        String accessToken,
        @NotBlank(message = "Refresh token is a required entry value")
        String refreshToken
) {
}
