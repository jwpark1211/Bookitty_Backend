package capstone.bookitty.domain.star.repository;

import capstone.bookitty.domain.star.domain.Star;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StarRepository extends JpaRepository<Star, Long>, StarCustomRepository {
    long countByIsbn(String isbn);
    List<Star> findByMemberIdAndScoreGreaterThanEqual(Long memberId, double score);
}
