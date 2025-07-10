package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.api.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.domain.vo.Password;
import capstone.bookitty.domain.member.exception.InvalidRefreshTokenException;
import capstone.bookitty.domain.member.fixture.MemberTestFixture;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.authentication.PasswordEncoder;
import capstone.bookitty.global.authentication.tokenDto.TokenRequest;
import capstone.bookitty.global.authentication.tokenDto.TokenResponse;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceTest {

    @Autowired
    MemberTestFixture memberFixture;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthService authService;

    @Nested
    @DisplayName("로그인 메서드 Test Cases")
    class LoginTest {

        @Test
        @DisplayName("로그인 성공 시 JWT 토큰을 발급합니다.")
        void shouldReturnJwtToken_whenLoginSucceeds() {
            // given
            String email = "testuser@example.com";
            String password = "Test1234!";
            String name = "홍길동";
            memberRepository.save(memberFixture.createMember().email(email)
                    .password(Password.fromRaw(password, passwordEncoder)).build());

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

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 BadCredentialsException이 발생합니다.")
        void shouldThrowException_whenEmailNotFound() {
            // given
            String email = "testuser@example.com";
            String password = "Test1234@";
            MemberLoginRequest request = new MemberLoginRequest(email, password);

            // when  + then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 BadCredentialsException이 발생합니다.")
        void shouldThrowException_whenPasswordInvalid() {
            // given
            String email = "testuser@example.com";
            String password = "Test1234@";
            memberRepository.save(memberFixture.createMember().email(email)
                    .password(Password.fromRaw(password, passwordEncoder)).build());

            MemberLoginRequest request = new MemberLoginRequest(email, "wrongPAssword!2@");

            // when  + then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("리프레시 토큰 재발급 메서드 Test Cases")
    class ReissueTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 JWT 토큰을 재발급합니다.")
        void successReissue() {
            // given
            String email = "testuser@example.com";
            String password = "Test1234@";
            String name = "홍길동";
            memberRepository.save(memberFixture.createMember().email(email)
                    .password(Password.fromRaw(password, passwordEncoder)).build());

            MemberLoginRequest loginRequest = new MemberLoginRequest(email, password);
            TokenResponse loginResponse = authService.login(loginRequest);

            // when
            TokenResponse reissueResponse = authService.reissue(
                    new TokenRequest(loginResponse.jwtToken().accessToken(), loginResponse.jwtToken().refreshToken()));

            // then
            assertThat(reissueResponse).isNotNull();
            assertThat(reissueResponse.idx()).isNotNull();
            assertThat(reissueResponse.name()).isEqualTo(name);
            assertThat(reissueResponse.jwtToken().accessToken()).isNotBlank();
            assertThat(reissueResponse.jwtToken().refreshToken()).isNotBlank();
        }

        @Test
        @DisplayName("유효하지 않은 refreshToken으로 재발급 요청 시 예외가 발생합니다.")
        void shouldThrowException_whenRefreshTokenIsInvalid() {
            // given
            String email = "testuser@example.com";
            String password = "Test1234@";
            memberRepository.save(memberFixture.createMember().email(email)
                    .password(Password.fromRaw(password, passwordEncoder)).build());

            MemberLoginRequest loginRequest = new MemberLoginRequest(email, password);
            TokenResponse loginResponse = authService.login(loginRequest);

            String tamperedRefreshToken = loginResponse.jwtToken().refreshToken() + "invalid";

            TokenRequest reissueRequest = new TokenRequest(loginResponse.jwtToken().accessToken(), tamperedRefreshToken);

            // when + then
            assertThatThrownBy(() -> authService.reissue(reissueRequest))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }
    }
}