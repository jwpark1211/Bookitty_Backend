package capstone.bookitty.domain.member.domain.vo;

import capstone.bookitty.global.authentication.PasswordEncoder;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PasswordTest {

    @Autowired
    PasswordEncoder encoder;

    @Nested
    @DisplayName("fromRaw() 테스트")
    class FromRawTest {

        @Test
        @DisplayName("성공: 유효한 평문 비밀번호 입력 시 생성됨")
        void success_when_valid_raw_password() {
            String raw = "Valid123!";
            Password password = Password.fromRaw(raw, encoder);

            assertThat(password.value()).startsWith("{bcrypt}$2a$");
        }

        @Test
        @DisplayName("실패: 공백 비밀번호")
        void fail_when_blank_password() {
            assertThatThrownBy(() -> Password.fromRaw(" ", encoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null or blank");
        }

        @Test
        @DisplayName("실패: 비밀번호 형식이 올바르지 않음")
        void fail_when_invalid_format() {
            assertThatThrownBy(() -> Password.fromRaw("abc123", encoder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Passwords must contain");
        }
    }

    @Nested
    @DisplayName("ofEncrypted() 테스트")
    class OfEncryptedTest {

        @Test
        @DisplayName("성공: bcrypt 포맷 암호문이면 생성 성공")
        void success_when_encrypted_valid_format() {
            String encrypted = encoder.encode("Valid123!");
            Password password = Password.ofEncrypted(encrypted);

            assertThat(password.value()).isEqualTo(encrypted);
        }

        @Test
        @DisplayName("실패: bcrypt 포맷이 아닌 경우")
        void fail_when_not_bcrypt_format() {
            assertThatThrownBy(() -> Password.ofEncrypted("plaintext"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expected bcrypt encoded password.");
        }
    }

    @Nested
    @DisplayName("동등성 비교")
    class EqualsAndHashCode {

        @Test
        @DisplayName("같은 암호문은 equals true")
        void equals_true_when_same_password() {

            String encrypted = encoder.encode("Valid123!");

            Password pw1 = Password.ofEncrypted(encrypted);
            Password pw2 = Password.ofEncrypted(encrypted);

            assertThat(pw1).isEqualTo(pw2);
            assertThat(pw1.hashCode()).isEqualTo(pw2.hashCode());
        }

        @Test
        @DisplayName("다른 암호문은 equals false")
        void equals_false_when_different_password() {
            Password pw1 = Password.fromRaw("Valid123!", encoder);
            Password pw2 = Password.fromRaw("Another123!", encoder);

            assertThat(pw1).isNotEqualTo(pw2);
        }
    }
}
