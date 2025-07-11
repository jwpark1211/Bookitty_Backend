package capstone.bookitty.domain.member.application.MockTest;

import capstone.bookitty.domain.member.application.RefreshTokenService;
import capstone.bookitty.domain.member.domain.RefreshToken;
import capstone.bookitty.domain.member.exception.InvalidRefreshTokenException;
import capstone.bookitty.domain.member.exception.NotLoggedInException;
import capstone.bookitty.domain.member.repository.RefreshTokenRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@Disabled("목테스트 - 공부용")
@ExtendWith(MockitoExtension.class)
class MockRefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private final String KEY = "user@email.com";
    private final String VALUE = "refresh-token";
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        refreshToken = new RefreshToken(KEY, VALUE);
    }

    @Nested
    @DisplayName("save() 테스트")
    class Save {

        @Test
        @DisplayName("리프레시 토큰 저장 성공")
        void 저장_성공() {
            // when
            refreshTokenService.save(KEY, VALUE);

            // then
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("validate() 테스트")
    class Validate {

        @Test
        @DisplayName("유효한 리프레시 토큰일 경우 검증 성공")
        void 검증_성공() {
            // given
            given(refreshTokenRepository.findByKey(KEY)).willReturn(Optional.of(refreshToken));

            // when / then
            assertThatCode(() -> refreshTokenService.validate(KEY, VALUE)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("토큰이 저장되어 있지 않으면 예외 발생")
        void 토큰없음_예외() {
            // given
            given(refreshTokenRepository.findByKey(KEY)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> refreshTokenService.validate(KEY, VALUE))
                    .isInstanceOf(NotLoggedInException.class);
        }

        @Test
        @DisplayName("저장된 토큰과 입력 토큰이 다르면 예외 발생")
        void 불일치_예외() {
            // given
            given(refreshTokenRepository.findByKey(KEY)).willReturn(Optional.of(refreshToken));

            // when / then
            assertThatThrownBy(() -> refreshTokenService.validate(KEY, "wrong-token"))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }
    }

    @Nested
    @DisplayName("update() 테스트")
    class Update {

        @Test
        @DisplayName("리프레시 토큰 업데이트 성공")
        void 업데이트_성공() {
            // given
            given(refreshTokenRepository.findByKey(KEY)).willReturn(Optional.of(refreshToken));

            // when
            refreshTokenService.update(KEY, "new-token");

            // then
            verify(refreshTokenRepository).save(any(RefreshToken.class));
            assertThat(refreshToken.getValue()).isEqualTo("new-token");
        }

        @Test
        @DisplayName("존재하지 않는 토큰 업데이트 시 예외 발생")
        void 업데이트_예외() {
            // given
            given(refreshTokenRepository.findByKey(KEY)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> refreshTokenService.update(KEY, "new-token"))
                    .isInstanceOf(NotLoggedInException.class);
        }
    }

    @Nested
    @DisplayName("delete() 테스트")
    class Delete {

        @Test
        @DisplayName("삭제 성공")
        void 삭제_성공() {
            // given
            given(refreshTokenRepository.findByKey(KEY)).willReturn(Optional.of(refreshToken));

            // when
            refreshTokenService.delete(KEY);

            // then
            verify(refreshTokenRepository).delete(refreshToken);
        }

        @Test
        @DisplayName("삭제할 토큰이 없으면 아무 일도 하지 않음")
        void 삭제할_토큰없음() {
            // given
            given(refreshTokenRepository.findByKey(KEY)).willReturn(Optional.empty());

            // when
            refreshTokenService.delete(KEY);

            // then
            verify(refreshTokenRepository, never()).delete(any());
        }
    }
}
