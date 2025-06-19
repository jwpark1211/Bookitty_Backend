package capstone.bookitty.domain.bookSimilarity.repository;

import capstone.bookitty.domain.bookSimilarity.domain.RatingEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingEventRepository extends JpaRepository<RatingEvent, Long> {
    List<RatingEvent> findByIsbnAndStatus(String isbn, RatingEvent.Status status);
    Page<RatingEvent> findByStatus(RatingEvent.Status status, Pageable pageable);
}
