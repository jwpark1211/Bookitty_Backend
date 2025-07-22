package capstone.bookitty.domain.bookSimilarity.repository;

import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookSimilarityRepository extends JpaRepository<BookSimilarity, BookSimilarityId> {
}
