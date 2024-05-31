package capstone.bookitty.domain.repository;

import capstone.bookitty.domain.entity.BookState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookStateRepository extends JpaRepository<BookState,Long> {
    Page<BookState> findByIsbn(String isbn, Pageable pageable);
    Page<BookState> findByMemberId(Long memberId,Pageable pageable);
    List<BookState> findByMemberId(Long memberId);
    boolean existsByMemberIdAndIsbn(Long memberId, String isbn);
    Optional<BookState> findByMemberIdAndIsbn(Long memberId, String isbn);

}