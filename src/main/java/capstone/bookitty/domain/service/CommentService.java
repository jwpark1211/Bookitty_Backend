package capstone.bookitty.domain.service;

import capstone.bookitty.domain.entity.Comment;
import capstone.bookitty.domain.entity.Like;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.repository.CommentRepository;
import capstone.bookitty.domain.repository.LikeRepository;
import capstone.bookitty.domain.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static capstone.bookitty.domain.dto.CommentDTO.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public IdResponse saveComment(CommentSaveRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(()->new EntityNotFoundException(
                        "Member with ID "+request.getMemberId()+" not found."));
        if(commentRepository.existsByMemberIdAndIsbn(request.getMemberId(),request.getIsbn()))
            throw new IllegalArgumentException("comment already exists.");

        Comment comment = Comment.builder()
                .member(member)
                .isbn(request.getIsbn())
                .content(request.getContent())
                .build();
        commentRepository.save(comment);

        return new IdResponse(comment.getId());
    }

    public CommentInfoResponse findCommentByCommentId(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID " + commentId + " not found."));
        int like_count = likeRepository.findByCommentId(commentId).size();
        Member member = memberRepository.findById(comment.getMember().getId())
                .orElseThrow(() -> new EntityNotFoundException("Member with commentID " + commentId + " not found."));
        return CommentInfoResponse.of(comment, like_count,member.getName(),member.getProfileImg());
    }

    public Page<CommentInfoResponse> findAllComment(Pageable pageable) {
        return commentRepository.findAll(pageable)
                .map(comment -> {
                    int like_count = likeRepository.findByCommentId(comment.getId()).size();
                    Member member = memberRepository.findById(comment.getMember().getId())
                            .orElseThrow(() -> new EntityNotFoundException("Member with commentID " + comment.getId() + " not found."));
                    return CommentInfoResponse.of(comment, like_count,member.getName(),member.getProfileImg());
                });
    }

    public Page<CommentInfoResponse> findCommentByIsbn(String isbn, Pageable pageable) {
        return commentRepository.findByIsbn(isbn,pageable)
                .map(comment -> {
                    int like_count = likeRepository.findByCommentId(comment.getId()).size();
                    Member member = memberRepository.findById(comment.getMember().getId())
                            .orElseThrow(() -> new EntityNotFoundException("Member with commentID " + comment.getId() + " not found."));
                    return CommentInfoResponse.of(comment, like_count,member.getName(),member.getProfileImg());
                });
    }

    public Page<CommentInfoResponse> findCommentByMemberId(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new EntityNotFoundException("Member with ID "+memberId+" not found."));
        return commentRepository.findByMemberId(memberId,pageable)
                .map(comment -> {
                    int like_count = likeRepository.findByCommentId(comment.getId()).size();
                    return CommentInfoResponse.of(comment, like_count,member.getName(),member.getProfileImg());
                });
    }

    @Transactional
    public void updateComment(Long commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID " + commentId + " not found."));
        comment.updateContent(request.getContent());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID " + commentId + " not found."));
        commentRepository.delete(comment);
    }

    @Transactional
    public void increaseLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID " + commentId + " not found."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new EntityNotFoundException("Member with ID "+memberId+" not found."));
        List<Like> likesByComment = likeRepository.findByCommentId(commentId);

        for(Like like : likesByComment){
            if(like.getMember().getId() == memberId)
                throw new IllegalArgumentException("like already exists");
        }

        Like like = Like.builder()
                .comment(comment)
                .member(member)
                .build();
        likeRepository.save(like);
    }

    @Transactional
    public void decreaseLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID " + commentId + " not found."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new EntityNotFoundException("Member with ID " + memberId + " not found."));
        Like like = likeRepository.findByMemberIdAndCommentId(memberId,commentId)
                        .orElseThrow(()->new EntityNotFoundException("Like with commentId "+commentId+" not found."));
        likeRepository.delete(like);
    }

    @Transactional
    public void deleteLike(Long likeId){
        Like like = likeRepository.findById(likeId)
                .orElseThrow(()->new EntityNotFoundException("Like with likeId "+likeId+" not found."));
        likeRepository.delete(like);
    }
}
