package capstone.bookitty.domain.star.dao;

import capstone.bookitty.domain.book.dto.BookPair;
import capstone.bookitty.domain.star.domain.Star;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StarRepository extends JpaRepository<Star, Long> {
    boolean existsByMemberIdAndIsbn(Long memberId, String isbn);
    Page<Star> findByIsbn(String isbn, Pageable pageable);
    Page<Star> findByMemberId(Long memberId, Pageable pageable);
    List<Star> findByMemberId(Long memberId);
    Optional<Star> findByMemberIdAndIsbn(Long memberId, String isbn);
    long countByIsbn(String isbn);


    // 같은 사용자가 평가한 도서 쌍을 BookPair DTO로 반환 (중복 제거)
    @Query("SELECT new capstone.bookitty.domain.dto.openApiDto.BookPair(s1.isbn, s2.isbn) " +
            "FROM Star s1 JOIN Star s2 ON s1.member.id = s2.member.id " +
            "WHERE s1.isbn < s2.isbn")
    Page<BookPair> findDistinctBookPairs(Pageable pageable);

    // 두 도서를 동시에 평가한 사용자의 평점 쌍을 반환 (각 행: [score1, score2])
    @Query("SELECT s1.score, s2.score " +
            "FROM Star s1 JOIN Star s2 ON s1.member.id = s2.member.id " +
            "WHERE s1.isbn = :isbn1 AND s2.isbn = :isbn2")
    List<Double[]> findCommonRatings(@Param("isbn1") String isbn1, @Param("isbn2") String isbn2);

}
