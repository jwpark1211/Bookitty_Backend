package capstone.bookitty.domain.star.repository;

import capstone.bookitty.domain.book.dto.BookPair;
import capstone.bookitty.domain.book.dto.RatingPair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StarCustomRepository {
    Page<BookPair> findDistinctBookPairs(Pageable pageable);
    List<RatingPair> findCommonRatings(String isbn1, String isbn2);
}
