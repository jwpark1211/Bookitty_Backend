package capstone.bookitty.domain.bookState.repository;

import capstone.bookitty.domain.bookState.domain.BookState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookStateCustomRepository {
    Page<BookState> findByFilters(String isbn, Long memberId, Pageable pageable);
}
