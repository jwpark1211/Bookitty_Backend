package capstone.bookitty.domain.star.repository;

import capstone.bookitty.domain.star.domain.Star;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StarRepository extends JpaRepository<Star, Long>, StarCustomRepository {
    long countByIsbn(String isbn);

    boolean existsByIsbnAndMemberId(String isbn, Long memberId);

    List<Star> findByMemberIdAndScoreGreaterThanEqual(Long memberId, double score);

    Page<Star> findByMemberId(Long memberId, Pageable pageable);

    Page<Star> findByIsbn(String isbn, Pageable pageable);
}
