package capstone.bookitty.domain.member.application;

import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.dto.MemberLoginRequest;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.exception.RefreshTokenSaveException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.authentication.JwtToken;
import capstone.bookitty.global.authentication.JwtTokenProvider;
import capstone.bookitty.global.authentication.RefreshToken;
import capstone.bookitty.global.authentication.RefreshTokenRepository;
import capstone.bookitty.global.authentication.tokenDto.TokenRequest;
import capstone.bookitty.global.authentication.tokenDto.TokenResponse;
import capstone.bookitty.global.error.exception.EntityNotFoundException;
import capstone.bookitty.global.util.RedisUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceAuthenticationTest {

    @InjectMocks
    private MemberService memberService;

    @Mock private MemberRepository memberRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private AuthenticationManagerBuilder authenticationManagerBuilder;
    @Mock private RedisUtil redisUtil;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private Authentication authentication;

    private static final String EMAIL = "test@example.com";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    private MemberLoginRequest loginRequest;
    private Member member;
    private JwtToken jwtToken;
    private RefreshToken refreshToken;
    private TokenRequest tokenRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new MemberLoginRequest(EMAIL, "password");
        member = Member.builder()
                .email(EMAIL)
                .name("테스트")
                .password("password")
                .profileImg("profile.jpg")
                .build();
        jwtToken = new JwtToken("Bearer", ACCESS_TOKEN, REFRESH_TOKEN);
        refreshToken = new RefreshToken(EMAIL, REFRESH_TOKEN);
        tokenRequest = new TokenRequest(ACCESS_TOKEN, REFRESH_TOKEN);
    }

    @Nested
    @DisplayName("로그인 테스트")
    class Login {
        @BeforeEach
        void setUpLogin() {
            given(authenticationManagerBuilder.getObject()).willReturn(authenticationManager);
            given(authenticationManager.authenticate(any())).willReturn(authentication);
            given(jwtTokenProvider.generateTokenDto(authentication)).willReturn(jwtToken);
        }

        @Test
        @DisplayName("로그인 성공 시 JWT 토큰 발급")
        void 로그인_성공() {
            //given
            given(memberRepository.findByEmail(EMAIL)).willReturn(Optional.of(member));
            given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(refreshToken);

            //when
            TokenResponse response = memberService.login(loginRequest);

            //then
            assertThat(response.idx()).isEqualTo(member.getId());
            assertThat(response.jwtToken().accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.jwtToken().refreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(response.name()).isEqualTo("테스트");
        }

        @Test
        @DisplayName("사용자 이메일로 회원 조회 실패 시 예외 발생")
        void 사용자없음_예외발생() {
            //given
            given(memberRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            //when + then
            assertThatThrownBy(() -> memberService.login(loginRequest))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("리프레시 토큰 저장 실패 시 예외 발생")
        void 리프레시토큰_저장실패() {
            //given
            given(memberRepository.findByEmail(EMAIL)).willReturn(Optional.of(member));
            given(refreshTokenRepository.save(any(RefreshToken.class)))
                    .willThrow(new RuntimeException("DB 오류"));

            //when + then
            assertThatThrownBy(() -> memberService.login(loginRequest))
                    .isInstanceOf(RefreshTokenSaveException.class);
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
            //given
            given(refreshTokenRepository.findByKey(EMAIL)).willReturn(Optional.of(refreshToken));
            given(jwtTokenProvider.generateTokenDto(authentication)).willReturn(newJwtToken);
            given(memberRepository.findByEmail(EMAIL)).willReturn(Optional.of(member));

            //when
            TokenResponse response = memberService.reissue(tokenRequest);

            //then
            assertThat(response.jwtToken().accessToken()).isEqualTo("new-access-token");
            assertThat(response.jwtToken().refreshToken()).isEqualTo("new-refresh-token");
            assertThat(response.idx()).isEqualTo(member.getId());
            assertThat(response.name()).isEqualTo("테스트");
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외 발생")
        void 회원_없음() {
            //given
            given(refreshTokenRepository.findByKey(EMAIL)).willReturn(Optional.of(refreshToken));
            given(jwtTokenProvider.generateTokenDto(authentication)).willReturn(newJwtToken);
            given(memberRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            //when + then
            assertThatThrownBy(() -> memberService.reissue(tokenRequest))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class Logout {

        @BeforeEach
        void setUpLogout() {
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getAuthentication(ACCESS_TOKEN)).willReturn(authentication);
            given(authentication.getName()).willReturn(EMAIL);
        }

        @Test
        @DisplayName("로그아웃 성공 시 RefreshToken 삭제 및 AccessToken 블랙리스트 등록")
        void 로그아웃_성공() {
            //given
            given(refreshTokenRepository.findByKey(EMAIL)).willReturn(Optional.of(refreshToken));
            given(jwtTokenProvider.getExpiration(ACCESS_TOKEN)).willReturn(3600L);

            //when
            memberService.logout(tokenRequest);

            //then
            verify(refreshTokenRepository).delete(refreshToken);
            verify(redisUtil).setBlackList(ACCESS_TOKEN, "access_token", 3600L);
        }
    }
}
