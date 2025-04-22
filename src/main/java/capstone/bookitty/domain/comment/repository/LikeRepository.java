package capstone.bookitty.domain.comment.repository;

import capstone.bookitty.domain.comment.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByCommentId(Long commentId);
    Optional<Like> findByMemberIdAndCommentId(Long memberId, Long commentId);
    List<Like> findByCommentIdIn(List<Long> commentIds);
}
