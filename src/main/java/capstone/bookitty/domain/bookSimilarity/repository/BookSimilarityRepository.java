package capstone.bookitty.domain.bookSimilarity.repository;

import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookSimilarityRepository extends JpaRepository<BookSimilarity, BookSimilarityId>{
    @Query(value = """
        (SELECT * FROM BookSimilarity WHERE isbn1 = :isbn)
        UNION ALL
        (SELECT * FROM BookSimilarity WHERE isbn2 = :isbn)
        ORDER BY similarity DESC
    """, nativeQuery = true)
    List<BookSimilarity> findTopSimilarBooks(@Param("isbn") String isbn);
}
