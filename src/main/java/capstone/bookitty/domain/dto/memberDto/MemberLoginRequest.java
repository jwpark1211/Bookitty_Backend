package capstone.bookitty.domain.dto.memberDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberLoginRequest (
        @NotBlank(message = "Email is a required entry value.")
        @Email(message = "Email format is not valid.")
        String email,
        @NotBlank(message = "Password is a required entry value.")
        String password
){
    public static MemberLoginRequest of(String email, String password){
        return new MemberLoginRequest(email, password);
    }
}
