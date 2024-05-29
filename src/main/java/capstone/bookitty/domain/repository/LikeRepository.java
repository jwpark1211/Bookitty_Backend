package capstone.bookitty.domain.repository;

import capstone.bookitty.domain.entity.Like;
import capstone.bookitty.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByCommentId(Long commentId);
    List<Like> findByMemberId(Long memberId);
    Optional<Like> findByMemberIdAndCommentId(Long memberId, Long commentId);
}
