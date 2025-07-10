package capstone.bookitty.domain.member.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record MemberLoginRequest(
        @NotBlank(message = "Email is a required entry value")
        @Email(message = "Email format is not valid")
        String email,
        @NotBlank(message = "Password is a required entry value")
        String password
) {
}
