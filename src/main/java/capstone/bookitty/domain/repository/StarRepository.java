package capstone.bookitty.domain.repository;

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
    Optional<Star> findByMemberIdAndIsbn(Long memberId, String isbn);
    @Query("SELECT s.isbn, SUM(s.score) as totalScore " +
            "FROM Star s " +
            "WHERE s.member.gender = :gender AND s.member.birthDate BETWEEN :startDate AND :endDate " +
            "GROUP BY s.isbn")
    List<Object[]> findTotalScoreByGenderAndBirthDate(@Param("gender") Gender gender,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
}
