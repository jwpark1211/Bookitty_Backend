package capstone.bookitty.domain.bookSimilarity.repository;

import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;

import java.util.List;

public interface BookSimilarityCustomRepository {
    List<BookSimilarity> findTopSimilarBooks(String isbn);
}
