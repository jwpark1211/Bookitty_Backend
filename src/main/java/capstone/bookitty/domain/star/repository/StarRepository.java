package capstone.bookitty.domain.star.repository;

import capstone.bookitty.domain.star.domain.Star;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


//TODO : JPQL 쓰는 쿼리는 customRepository로 분리하는 게 좋을듯 !
@Repository
public interface StarRepository extends JpaRepository<Star, Long> {

    boolean existsByIsbnAndMemberId(String isbn, Long memberId);

    Page<Star> findByMemberId(Long memberId, Pageable pageable);

    Page<Star> findByIsbn(String isbn, Pageable pageable);

    List<Star> findByIsbn(String isbn);

    @Query("""
            SELECT s.isbn 
            FROM Star s 
            GROUP BY s.isbn 
            HAVING COUNT(s) >= :minCount
            """)
    List<String> findIsbnsWithMinimumRatings(@Param("minCount") int minCount);

}
