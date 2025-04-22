package capstone.bookitty.domain.bookState.repository;

import capstone.bookitty.domain.bookState.domain.BookState;
import capstone.bookitty.domain.member.domain.Gender;
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
public interface BookStateRepository extends JpaRepository<BookState,Long>,BookStateCustomRepository {
    List<BookState> findByMemberId(Long memberId);
}