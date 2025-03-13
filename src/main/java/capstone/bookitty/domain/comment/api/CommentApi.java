package capstone.bookitty.domain.comment.api;


import capstone.bookitty.domain.comment.dto.CommentInfoResponse;
import capstone.bookitty.domain.comment.dto.CommentSaveRequest;
import capstone.bookitty.domain.comment.dto.CommentUpdateRequest;
import capstone.bookitty.global.dto.IdResponse;
import capstone.bookitty.domain.comment.application.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Tag(name="comment", description = "댓글 관리 api 입니다.")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentApi {

    private final CommentService commentService;

    @Operation(summary = "코멘트 생성 / content는 최소 1자, 최대 100자")
    @PostMapping
    public ResponseEntity<IdResponse> createComment(
            @RequestBody @Valid final CommentSaveRequest request
    ){
        IdResponse response = commentService.saveComment(request);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "commentId로 코멘트 가져오기")
    @GetMapping(path="/{comment-id}")
    public ResponseEntity<CommentInfoResponse> getCommentById(
            @PathVariable("comment-id") Long commentId
    ){
        CommentInfoResponse response = commentService.findCommentByCommentId(commentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "코멘트 가져오기(전체, isbn, memberId 중 선택)")
    @GetMapping
    public ResponseEntity<Page<CommentInfoResponse>> getComments(
            @RequestParam(name = "isbn", required = false) String isbn,
            @RequestParam(name = "memberId", required = false) Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable
    ){
        Page<CommentInfoResponse> responseList = commentService.findComments(isbn, memberId, pageable);
        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "코멘트 수정 / content는 최소 1자, 최대 100자")
    @PatchMapping(path = "/{comment-id}")
    public ResponseEntity<Void> updateComment(
            @PathVariable("comment-id") Long commentId,
            @RequestBody @Valid CommentUpdateRequest request
    ){
        commentService.updateComment(commentId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "코멘트 삭제")
    @DeleteMapping(path = "/{comment-id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("comment-id") Long commentId
    ){
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "코멘트 좋아요 등록")
    @PostMapping(path = "/{comment-id}/likes")
    public ResponseEntity<Void> likeComment(
            @PathVariable("comment-id") Long commentId,
            @PathVariable("member-id") Long memberId
    ){
        commentService.increaseLike(commentId,memberId);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "코멘트 좋아요 삭제")
    @DeleteMapping(path = "/{comment-id}/likes")
    public ResponseEntity<Void> decreaseLike(
            @PathVariable("comment-id") Long commentId,
            @PathVariable("member-id") Long memberId
    ){
        commentService.decreaseLike(commentId,memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "좋아요 id로 삭제")
    @DeleteMapping(path = "/likes/{like-id}")
    public ResponseEntity<Void> deleteLike(
            @PathVariable("like-id") Long likeId
    ){
        commentService.deleteLike(likeId);
        return ResponseEntity.noContent().build();
    }

}
