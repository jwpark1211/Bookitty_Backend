package capstone.bookitty.domain.bookSimilarity.similarityBatch.item;

import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.dto.BookPairDto;
import capstone.bookitty.domain.star.repository.StarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookSimilarityReader implements ItemReader<BookPairDto> {

    private final StarRepository starRepository;
    private Iterator<BookPairDto> bookPairIterator;
    private boolean initialized = false;

    @Override
    public BookPairDto read() {
        if (!initialized) {
            initializeBookPairs();
            initialized = true;
        }

        return bookPairIterator.hasNext() ? bookPairIterator.next() : null;
    }

    private void initializeBookPairs() {
        log.info("=== 책 간 유사도 계산을 위한 책 쌍 초기화 시작 ===");

        // 평점이 3개 이상 있는 책들만 조회 (유의미한 유사도 계산을 위해)
        List<String> validIsbns = starRepository.findIsbnsWithMinimumRatings(3);
        log.info("유효한 책 수 (평점 3개 이상): {} 권", validIsbns.size());

        if (validIsbns.size() < 2) {
            log.warn("유사도 계산할 책이 부족합니다. 최소 2권 이상 필요합니다.");
            this.bookPairIterator = Collections.emptyIterator();
            return;
        }

        // 모든 책 쌍 생성
        List<BookPairDto> bookPairs = generateBookPairs(validIsbns);
        log.info("생성된 책 쌍 수: {} 개", bookPairs.size());
        log.info("=== 책 쌍 초기화 완료 ===");

        this.bookPairIterator = bookPairs.iterator();
    }

    private List<BookPairDto> generateBookPairs(List<String> isbns) {
        List<BookPairDto> pairs = new ArrayList<>();

        for (int i = 0; i < isbns.size(); i++) {
            for (int j = i + 1; j < isbns.size(); j++) {
                pairs.add(BookPairDto.of(isbns.get(i), isbns.get(j)));
            }
        }

        return pairs;
    }
}