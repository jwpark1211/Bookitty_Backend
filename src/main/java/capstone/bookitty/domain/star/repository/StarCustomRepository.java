package capstone.bookitty.domain.star.repository;

import capstone.bookitty.domain.book.api.dto.RatingPair;

import java.util.List;

public interface StarCustomRepository {
    List<RatingPair> findCommonRatings(String isbn1, String isbn2);

    List<String> findIsbnsRatedWith(String isbn);
}
