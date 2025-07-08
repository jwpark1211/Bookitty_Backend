package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.fixture.MemberTestFixture;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.authentication.PasswordEncoder;
import capstone.bookitty.global.authentication.tokenDto.TokenResponse;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;



import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceTest {
    @Autowired MemberTestFixture memberFixture;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired AuthService authService;

    @Nested
    @DisplayName("로그인 메서드 Test Cases")
    class LoginTest{
        @Test
        @DisplayName("로그인 성공 시 JWT 토큰 발급")
        void successLogin() {
            // given
            String email = "testuser@example.com";
            String password = "Test1234@";
            String name = "홍길동";
            String encodedPassword = passwordEncoder.encode(password);
            memberRepository.save(memberFixture.createMember().email(email).encodedPassword(encodedPassword).build());

            MemberLoginRequest request = new MemberLoginRequest(email, password);

            // when
            TokenResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.idx()).isNotNull();
            assertThat(response.name()).isEqualTo(name);
            assertThat(response.jwtToken().accessToken()).isNotBlank();
            assertThat(response.jwtToken().refreshToken()).isNotBlank();
        }
    }

}