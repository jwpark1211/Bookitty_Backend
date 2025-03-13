package capstone.bookitty.domain.comment.repository;

import capstone.bookitty.domain.comment.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentCustomRepository {
    Page<Comment> findByFilters(String isbn, Long memberId, Pageable pageable);
}
