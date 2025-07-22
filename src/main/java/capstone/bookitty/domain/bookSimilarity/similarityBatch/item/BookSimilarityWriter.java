package capstone.bookitty.domain.bookSimilarity.similarityBatch.item;

import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.repository.BookSimilarityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookSimilarityWriter implements ItemWriter<BookSimilarity> {

    private final BookSimilarityRepository bookSimilarityRepository;

    @Override
    public void write(Chunk<? extends BookSimilarity> chunk) {
        // null이 아닌 유효한 유사도 데이터만 필터링
        List<BookSimilarity> validSimilarities = chunk.getItems().stream()
                .filter(similarity -> similarity != null)
                .collect(Collectors.toList());

        if (validSimilarities.isEmpty()) {
            log.debug("저장할 유효한 유사도 데이터가 없습니다.");
            return;
        }

        bookSimilarityRepository.saveAll(validSimilarities);
        log.info("유사도 데이터 저장 완료: {}개", validSimilarities.size());
    }

}
