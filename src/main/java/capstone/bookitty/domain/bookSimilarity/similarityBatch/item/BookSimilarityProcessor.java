package capstone.bookitty.domain.bookSimilarity.similarityBatch.item;

import capstone.bookitty.domain.bookSimilarity.application.BookSimilarityService;
import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.dto.BookPairDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookSimilarityProcessor implements ItemProcessor<BookPairDto, BookSimilarity> {

    private final BookSimilarityService bookSimilarityService;

    @Override
    public BookSimilarity process(BookPairDto bookPair) {
        // Redis 캐시를 사용하므로 로컬 캐시 제거
        return bookSimilarityService.calculateAndSaveSimilarity(
            bookPair.isbn1(), 
            bookPair.isbn2()
        );
    }

}