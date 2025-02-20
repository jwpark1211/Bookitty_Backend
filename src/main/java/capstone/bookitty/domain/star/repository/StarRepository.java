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
    boolean existsByMemberIdAndIsbn(Long memberId, String isbn);
    Page<Star> findByIsbn(String isbn, Pageable pageable);
    Page<Star> findByMemberId(Long memberId, Pageable pageable);
    List<Star> findByMemberId(Long memberId);
    Optional<Star> findByMemberIdAndIsbn(Long memberId, String isbn);
    long countByIsbn(String isbn);

}
