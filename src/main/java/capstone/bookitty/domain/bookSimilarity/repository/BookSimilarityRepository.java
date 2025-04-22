package capstone.bookitty.domain.bookSimilarity.repository;

import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface BookSimilarityRepository extends JpaRepository<BookSimilarity, BookSimilarityId>{
    @Query(value = """
        (SELECT * FROM BookSimilarity WHERE isbn1 = :isbn)
        UNION ALL
        (SELECT * FROM BookSimilarity WHERE isbn2 = :isbn)
        ORDER BY similarity DESC
    """, nativeQuery = true)
    List<BookSimilarity> findTopSimilarBooks(@Param("isbn") String isbn);
    // 사용자가 평가한 ISBN 목록에 포함된 모든 유사도 관계를 한 번에 읽어옵니다.
    List<BookSimilarity> findByIsbn1InOrIsbn2In(Collection<String> isbn1List, Collection<String> isbn2List);
}
