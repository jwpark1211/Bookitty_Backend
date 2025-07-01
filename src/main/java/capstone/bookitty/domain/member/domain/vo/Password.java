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
            throw new IllegalArgumentException("비밀번호는 비어있을 수 없습니다.");
        }

        // 영문 대소문자 + 숫자 + 특수문자 1개 이상씩 포함, 길이 8~20자
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$")) {
            throw new IllegalArgumentException("비밀번호는 영문 대소문자, 숫자, 특수문자를 각각 1개 이상 포함하고, 길이는 8자 이상 20자 이하여야 합니다.");
        }
    }

    public String encode(PasswordEncoder encoder) {
        return encoder.encode(rawPassword);
    }
}
