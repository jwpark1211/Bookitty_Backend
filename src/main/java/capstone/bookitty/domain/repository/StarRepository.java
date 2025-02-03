package capstone.bookitty.domain.repository;

import capstone.bookitty.domain.dto.openApiDto.BookPair;
import capstone.bookitty.domain.entity.BookState;
import capstone.bookitty.domain.entity.Gender;
import capstone.bookitty.domain.entity.Star;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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
    List<Object[]> findCommonRatings(@Param("isbn1") String isbn1, @Param("isbn2") String isbn2);

    /*@Query("SELECT s.isbn, SUM(s.score) as totalScore " +
            "FROM Star s " +
            "WHERE s.member.gender = :gender AND s.member.birthDate BETWEEN :startDate AND :endDate " +
            "GROUP BY s.isbn")
    List<Object[]> findTotalScoreByGenderAndBirthDate(@Param("gender") Gender gender,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);*/
}
