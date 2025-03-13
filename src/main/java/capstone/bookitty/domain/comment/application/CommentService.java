package capstone.bookitty.domain.comment.application;

import capstone.bookitty.domain.comment.dto.CommentInfoResponse;
import capstone.bookitty.domain.comment.dto.CommentSaveRequest;
import capstone.bookitty.domain.comment.dto.CommentUpdateRequest;
import capstone.bookitty.domain.comment.dto.CommentUpdateResponse;
import capstone.bookitty.global.dto.IdResponse;
import capstone.bookitty.domain.comment.domain.Comment;
import capstone.bookitty.domain.comment.domain.Like;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.comment.exception.CommentNotFoundException;
import capstone.bookitty.domain.comment.exception.LikeNotFoundException;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.comment.repository.CommentRepository;
import capstone.bookitty.domain.comment.repository.LikeRepository;
import capstone.bookitty.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public IdResponse saveComment(CommentSaveRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(()->new MemberNotFoundException(request.memberId()));
        if(commentRepository.existsByMemberIdAndIsbn(request.memberId(),request.isbn()))
            throw new IllegalArgumentException("comment already exists.");

        Comment comment = Comment.builder()
                .member(member)
                .isbn(request.isbn())
                .content(request.content())
                .build();
        commentRepository.save(comment);

        return new IdResponse(comment.getId());
    }

    @Transactional(readOnly = true)
    public CommentInfoResponse findCommentByCommentId(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        int like_count = likeRepository.findByCommentId(commentId).size();
        Long memberId = comment.getMember().getId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        return CommentInfoResponse.of(comment, like_count,member.getName(),member.getProfileImg());
    }

    @Transactional
    public CommentUpdateResponse updateComment(Long commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        comment.updateContent(request.content());
        return new CommentUpdateResponse(comment.getId(), comment.getContent(), comment.getModifiedAt());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        commentRepository.delete(comment);
    }

    @Transactional
    public void increaseLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
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
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new MemberNotFoundException(memberId));
        Like like = likeRepository.findByMemberIdAndCommentId(memberId,commentId)
                        .orElseThrow(()->new CommentNotFoundException(commentId));
        likeRepository.delete(like);
    }

    @Transactional
    public void deleteLike(Long likeId){
        Like like = likeRepository.findById(likeId)
                .orElseThrow(()->new LikeNotFoundException(likeId));
        likeRepository.delete(like);
    }

    @Transactional(readOnly = true)
    public Page<CommentInfoResponse> findComments(String isbn, Long memberId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByFilters(isbn, memberId, pageable);
        List<Long> commentIds = comments.stream().map(Comment::getId).toList();

        List<Like> likes = commentIds.isEmpty() ? List.of() : likeRepository.findByCommentIdIn(commentIds);
        Map<Long, Integer> likeCountMap = likes.stream()
                .collect(Collectors.groupingBy(like -> like.getComment().getId(), Collectors.summingInt(like -> 1)));

        return comments.map(comment -> {
            int likeCount = likeCountMap.getOrDefault(comment.getId(), 0);
            //NPE 방지
            String memberName = comment.getMember() != null ? comment.getMember().getName() : "Unknown";
            String memberProfileImg = comment.getMember() != null ? comment.getMember().getProfileImg() : null;
            return CommentInfoResponse.of(comment, likeCount, memberName, memberProfileImg);
        });
    }

}
