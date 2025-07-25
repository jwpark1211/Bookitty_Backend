package capstone.bookitty.domain.star.repository;

import capstone.bookitty.domain.star.domain.Star;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StarRepository extends JpaRepository<Star, Long>, StarCustomRepository {

    boolean existsByIsbnAndMemberId(String isbn, Long memberId);

    Page<Star> findByMemberId(Long memberId, Pageable pageable);

    Page<Star> findByIsbn(String isbn, Pageable pageable);

    List<Star> findByIsbn(String isbn);

}
