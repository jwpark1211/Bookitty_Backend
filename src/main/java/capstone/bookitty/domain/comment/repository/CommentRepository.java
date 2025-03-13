package capstone.bookitty.domain.comment.repository;

import capstone.bookitty.domain.comment.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentCustomRepository{
    boolean existsByMemberIdAndIsbn(Long memberId, String isbn);
    Page<Comment> findAllByIsbnAndMemberId(String isbn, Long memberId, Pageable pageable);
    Page<Comment> findByMemberId(Long memberId, Pageable pageable);
    Page<Comment> findByIsbn(String isbn, Pageable pageable);
}
