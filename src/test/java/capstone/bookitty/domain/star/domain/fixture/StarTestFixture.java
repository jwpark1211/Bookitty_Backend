package capstone.bookitty.domain.star.domain.fixture;

import capstone.bookitty.domain.star.api.dto.StarSaveRequest;
import capstone.bookitty.domain.star.api.dto.StarUpdateRequest;
import capstone.bookitty.domain.star.domain.Star;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

//FIXME : Member 객체를 BeforeEach 혹은 PostConstruct로 미리 생성해두면 다른 테스트에 영향을 줄까?
@Component
@RequiredArgsConstructor
public class StarTestFixture {

    private static final String isbn = "9788936439743";
    private static final double score = 4.5;

    public StarSaveRequest.StarSaveRequestBuilder createStarSaveRequest() {
        return StarSaveRequest.builder()
                .memberId(1L)
                .isbn(isbn)
                .score(score);
    }

    public Star.StarBuilder createStar() {
        return Star.builder()
                .memberId(1L)
                .isbn(isbn)
                .score(score);
    }

    public StarUpdateRequest.StarUpdateRequestBuilder createStarUpdateRequest() {
        return StarUpdateRequest.builder()
                .score(score);
    }
}
