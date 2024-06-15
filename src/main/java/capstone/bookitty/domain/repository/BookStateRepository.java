package capstone.bookitty.domain.repository;

import capstone.bookitty.domain.entity.BookState;
import capstone.bookitty.domain.entity.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookStateRepository extends JpaRepository<BookState,Long> {
    Page<BookState> findByIsbn(String isbn, Pageable pageable);
    Page<BookState> findByMemberId(Long memberId,Pageable pageable);
    List<BookState> findByMemberId(Long memberId);
    boolean existsByMemberIdAndIsbn(Long memberId, String isbn);
    Optional<BookState> findByMemberIdAndIsbn(Long memberId, String isbn);
    @Query("SELECT b.isbn, COUNT(b) as stateCount " +
            "FROM BookState b " +
            "WHERE b.member.gender = :gender AND b.member.birthDate BETWEEN :startDate AND :endDate " +
            "GROUP BY b.isbn")
    List<Object[]> findStateCountByGenderAndBirthDate(@Param("gender") Gender gender,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

}