package capstone.bookitty.domain.star.domain;

import capstone.bookitty.domain.star.domain.fixture.StarTestFixture;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StarTest {

    @Autowired
    private StarTestFixture starFixture;

    @Nested
    @DisplayName("별점 생성자 Test Cases")
    class CreateStar {

        private static final String VALID_ISBN = "9783161484100";
        private static final double VALID_SCORE = 5;

        @Test
        @DisplayName("필수값이 유효하면 정상적으로 생성된다.")
        void success_when_valid_inputs() {
            Star star = starFixture.createStar()
                    .memberId(1L)
                    .isbn(VALID_ISBN)
                    .score(VALID_SCORE)
                    .build();

            assertThat(star.getIsbn()).isEqualTo(VALID_ISBN);
            assertThat(star.getScore()).isEqualTo(VALID_SCORE);
        }

        @Test
        @DisplayName("isbn이 공백인 경우 예외가 발생한다.")
        void fail_when_isbn_blank() {
            assertThatThrownBy(() -> starFixture.createStar()
                    .isbn(" ")
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ISBN cannot be null or empty");
        }

        @Test
        @DisplayName("점수가 0보다 작은 경우 예외가 발생한다.")
        void fail_when_score_smaller_than_zero() {
            assertThatThrownBy(() -> starFixture.createStar()
                    .score(-1.0)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Score must be between 0.5 and 5.0");
        }

        @Test
        @DisplayName("점수가 5보다 큰 경우 예외가 발생한다.")
        void fail_when_score_greater_than_five() {
            assertThatThrownBy(() -> starFixture.createStar()
                    .score(6.0)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Score must be between 0.5 and 5.0");
        }

        @Test
        @DisplayName("실패: 점수가 0.5 단위가 아닐 경우 예외가 발생한다")
        void fail_when_score_not_half_unit() {
            assertThatThrownBy(() -> starFixture.createStar()
                    .score(2.3)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Score must be in 0.5 increments");
        }
    }
}