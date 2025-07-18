package capstone.bookitty.domain.member.domain;

import capstone.bookitty.domain.member.domain.type.Authority;
import capstone.bookitty.domain.member.fixture.MemberTestFixture;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberTest {

    @Autowired
    MemberTestFixture memberFixture;

    @Nested
    @DisplayName("회원 생성자 Test Cases")
    class CreateMember {

        private static final String VALID_NAME = "홍길동";
        private static final String VALID_EMAIL = "valid@gmail.com";

        @Test
        @DisplayName("필수값이 유효하면 정상적으로 생성된다.")
        void success_when_valid_inputs() {
            Member member = memberFixture.createMember()
                    .name(VALID_NAME).email(VALID_EMAIL).build();

            assertThat(member.getName()).isEqualTo(VALID_NAME);
            assertThat(member.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(member.getAuthority()).isEqualTo(Authority.ROLE_USER);
            assertThat(member.getProfileImg()).isNotBlank();
        }

        @Test
        @DisplayName("이름이 공백일 경우 예외가 발생한다.")
        void fail_when_name_blank() {
            assertThatThrownBy(() -> memberFixture.createMember()
                    .name(" ")
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Name must not be blank");
        }

        @Test
        @DisplayName("이름이 10자를 초과할 경우 예외가 발생한다.")
        void fail_when_name_too_long() {
            assertThatThrownBy(() -> memberFixture.createMember()
                    .name("ABCDEFGHIJK")
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Name must not exceed 10 characters");
        }

        @Test
        @DisplayName("이메일 형식이 잘못된 경우 예외가 발생한다.")
        void fail_when_email_invalid() {
            assertThatThrownBy(() -> memberFixture.createMember()
                    .email("invalid-email")
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email format is invalid");
        }

        @Test
        @DisplayName("생년월일이 미래인 경우 예외가 발생한다.")
        void fail_when_birthDate_in_future() {
            assertThatThrownBy(() -> memberFixture.createMember()
                    .birthDate(LocalDate.now().plusDays(1))
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Birth date must be a date in the past");
        }

        @Test
        @DisplayName("성별 정보가 없는 경우 예외가 발생한다.")
        void fail_when_gender_null() {
            assertThatThrownBy(() -> memberFixture.createMember()
                    .gender(null)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Gender is required");
        }
    }

    @Nested
    @DisplayName("권한 검증 Test Cases")
    class PermissionValidation {

        @Test
        @DisplayName("동일한 사용자에 접근할 때 권한이 있다.")
        void success_when_same_user() {
            Member member1 = createMemberWithId(1L, Authority.ROLE_USER);
            Member member2 = createMemberWithId(1L, Authority.ROLE_USER);

            assertThatCode(() -> member1.validatePermissionTo(member2)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("관리자 사용자가 다른 사용자에 접근할 때 권한이 있다.")
        void success_when_admin_user() {
            Member admin = createMemberWithId(99L, Authority.ROLE_ADMIN);
            Member target = createMemberWithId(100L, Authority.ROLE_USER);

            assertThatCode(() -> admin.validatePermissionTo(target)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("일반 사용자가 다른 사용자에 접근할 때 권한이 없다.")
        void fail_when_other_user_and_not_admin() {
            Member user1 = createMemberWithId(1L, Authority.ROLE_USER);
            Member user2 = createMemberWithId(2L, Authority.ROLE_USER);

            assertThatThrownBy(() -> user1.validatePermissionTo(user2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Access denied");
        }

        //== Helper Method ==//

        private Member createMemberWithId(Long id, Authority authority) {
            Member member = memberFixture.createMember()
                    .authority(authority)
                    .build();

            // id를 임의로 세팅 (Reflection으로 강제 주입)
            try {
                var field = Member.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(member, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return member;
        }
    }
}
