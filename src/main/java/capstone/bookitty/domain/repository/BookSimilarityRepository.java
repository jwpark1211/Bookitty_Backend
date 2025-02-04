package capstone.bookitty.domain.repository;

import capstone.bookitty.domain.entity.BookSimilarity;
import capstone.bookitty.domain.entity.BookSimilarityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookSimilarityRepository extends JpaRepository<BookSimilarity, BookSimilarityId> {
    @Query("SELECT bs FROM BookSimilarity bs WHERE bs.isbn1 = :isbn OR bs.isbn2 = :isbn ORDER BY bs.similarity DESC")
    List<BookSimilarity> findTopSimilarBooks(@Param("isbn") String isbn);
}
