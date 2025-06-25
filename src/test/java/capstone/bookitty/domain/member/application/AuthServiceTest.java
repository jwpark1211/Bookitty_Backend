package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.domain.Gender;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.authentication.JwtToken;
import capstone.bookitty.global.authentication.JwtTokenProvider;
import capstone.bookitty.global.authentication.tokenDto.TokenRequest;
import capstone.bookitty.global.authentication.tokenDto.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private MemberRepository memberRepository;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private Authentication authentication;

    private static final String EMAIL = "test@example.com";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    private MemberLoginRequest loginRequest;
    private Member member;
    private JwtToken jwtToken;
    private TokenRequest tokenRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new MemberLoginRequest(EMAIL, "!Passwordw23");

        member = Member.create("테스트", EMAIL, "!Passwordw23",
                "profile.jpg", Gender.MALE, LocalDate.of(2001,12,11));
        member.setId(1L);

        jwtToken = new JwtToken("Bearer", ACCESS_TOKEN, REFRESH_TOKEN);
        tokenRequest = new TokenRequest(ACCESS_TOKEN, REFRESH_TOKEN);
    }

    @Nested
    @DisplayName("로그인 테스트")
    class Login {

        @BeforeEach
        void setUpLogin() {
            given(authenticationManager.authenticate(any()))
                    .willReturn(authentication);
            given(jwtTokenProvider.generateTokenDto(authentication))
                    .willReturn(jwtToken);
            given(authentication.getName()).willReturn(EMAIL);
        }

        @Test
        @DisplayName("로그인 성공 시 JWT 토큰 발급")
        void 로그인_성공() {
            // given
            given(memberRepository.findByEmail(EMAIL)).willReturn(Optional.of(member));
            willDoNothing().given(refreshTokenService).save(EMAIL, REFRESH_TOKEN);

            // when
            TokenResponse response = authService.login(loginRequest);

            // then
            assertThat(response.idx()).isEqualTo(member.getId());
            assertThat(response.jwtToken().accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.jwtToken().refreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(response.name()).isEqualTo("테스트");
        }

        @Test
        @DisplayName("사용자 이메일로 회원 조회 실패 시 예외 발생")
        void 사용자없음_예외발생() {
            // given
            given(memberRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(MemberNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("토큰 재발급 테스트")
    class Reissue {

        private JwtToken newJwtToken;

        @BeforeEach
        void setUpReissue() {
            newJwtToken = new JwtToken("Bearer", "new-access-token", "new-refresh-token");

            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getAuthentication(ACCESS_TOKEN)).willReturn(authentication);
            given(authentication.getName()).willReturn(EMAIL);
        }

        @Test
        @DisplayName("성공 시 새로운 토큰 반환")
        void 재발급_성공() {
            // given
            willDoNothing().given(refreshTokenService).validate(EMAIL, REFRESH_TOKEN);
            given(jwtTokenProvider.generateTokenDto(authentication)).willReturn(newJwtToken);
            willDoNothing().given(refreshTokenService).update(EMAIL, "new-refresh-token");
            given(memberRepository.findByEmail(EMAIL)).willReturn(Optional.of(member));

            // when
            TokenResponse response = authService.reissue(tokenRequest);

            // then
            assertThat(response.jwtToken().accessToken()).isEqualTo("new-access-token");
            assertThat(response.jwtToken().refreshToken()).isEqualTo("new-refresh-token");
            assertThat(response.idx()).isEqualTo(member.getId());
            assertThat(response.name()).isEqualTo("테스트");
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외 발생")
        void 회원_없음() {
            // given
            willDoNothing().given(refreshTokenService).validate(EMAIL, REFRESH_TOKEN);
            given(jwtTokenProvider.generateTokenDto(authentication)).willReturn(newJwtToken);
            willDoNothing().given(refreshTokenService).update(EMAIL, "new-refresh-token");
            given(memberRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> authService.reissue(tokenRequest))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }

}
