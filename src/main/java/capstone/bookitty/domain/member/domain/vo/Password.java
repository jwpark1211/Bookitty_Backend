package capstone.bookitty.domain.member.domain.vo;

import capstone.bookitty.global.authentication.PasswordEncoder;
import jakarta.persistence.Embeddable;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Embeddable
public class Password {

    private String password;

    protected Password() {
    }

    private Password(String encodedPassword) {
        this.password = encodedPassword;
    }

    public static Password fromRaw(String raw, PasswordEncoder encoder) {
        validate(raw);
        return new Password(encoder.encode(raw));
    }

    public static Password ofEncrypted(String encrypted) {
        if (encrypted == null || !encrypted.startsWith("{bcrypt}$2a$")) {
            throw new IllegalArgumentException("Expected bcrypt encoded password.");
        }
        return new Password(encrypted);
    }

    public String value() {
        return password;
    }

    private static void validate(String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$")) {
            throw new IllegalArgumentException("Passwords must contain at least one English case, number, and special characters, and must be 8 or more and 20 or less in length");
        }
    }

    public Password changePassword(String currentRawPassword, String newRawPassword, PasswordEncoder encoder) {
        if (!encoder.matches(currentRawPassword, this.password)) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        return Password.fromRaw(newRawPassword, encoder);
    }

    // equals, hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Password that)) return false;
        return Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(password);
    }
}
