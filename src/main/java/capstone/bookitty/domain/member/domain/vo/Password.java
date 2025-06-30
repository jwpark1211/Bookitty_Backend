package capstone.bookitty.domain.member.domain.vo;

import org.springframework.security.crypto.password.PasswordEncoder;


public class Password {
    private final String rawPassword;

    public Password(String rawPassword) {
        validate(rawPassword);
        this.rawPassword = rawPassword;
    }

    private void validate(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank.");
        }

        // 영문 대소문자 + 숫자 + 특수문자 1개 이상씩 포함, 길이 8~20자
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character, and be 8-20 characters long.");
        }
    }

    public String encode(PasswordEncoder encoder) {
        return encoder.encode(rawPassword);
    }
}
