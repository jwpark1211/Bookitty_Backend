package capstone.bookitty.domain.controller;


import capstone.bookitty.domain.dto.commentDto.CommentInfoResponse;
import capstone.bookitty.domain.dto.commentDto.CommentSaveRequest;
import capstone.bookitty.domain.dto.commentDto.CommentUpdateRequest;
import capstone.bookitty.domain.dto.commentDto.CommentUpdateResponse;
import capstone.bookitty.domain.dto.commonDto.IdResponse;
import capstone.bookitty.domain.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name="코멘트", description = "코멘트 관리 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "코멘트 생성 / content는 최소 1자, 최대 100자")
    @PostMapping(path = "/new")
    public ResponseEntity<IdResponse> saveComment(
            @RequestBody @Valid final CommentSaveRequest request
    ){
        IdResponse response = commentService.saveComment(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "commentId로 코멘트 가져오기")
    @GetMapping(path="/{comment-id}")
    public ResponseEntity<CommentInfoResponse> getCommentById(
            @PathVariable("comment-id") Long commentId
    ){
        CommentInfoResponse response = commentService.findCommentByCommentId(commentId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "전체 코멘트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
                        "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/all")
    public ResponseEntity<Page<CommentInfoResponse>> getAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<CommentInfoResponse> responseList = commentService.findAllComment(pageable);
        return ResponseEntity.ok().body(responseList);
    }

    @Operation(summary = "isbn으로 코멘트 리스트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/isbn/{isbn}")
    public ResponseEntity<Page<CommentInfoResponse>> getCommentByISBN(
            @PathVariable("isbn") String isbn,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<CommentInfoResponse> responseList = commentService.findCommentByIsbn(isbn,pageable);
        return ResponseEntity.ok().body(responseList);
    }

    @Operation(summary = "memberId로 코멘트 리스트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/member/{member-id}")
    public ResponseEntity<Page<CommentInfoResponse>> getCommentByMemberId(
            @PathVariable("member-id") Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<CommentInfoResponse> responseList = commentService.findCommentByMemberId(memberId, pageable);
        return ResponseEntity.ok().body(responseList);
    }

    @Operation(summary = "코멘트 수정 / content는 최소 1자, 최대 100자")
    @PatchMapping(path = "/{comment-id}")
    public void updateComment(
            @PathVariable("comment-id") Long commentId,
            @RequestBody @Valid CommentUpdateRequest request
    ){
        commentService.updateComment(commentId, request);
    }

    @Operation(summary = "코멘트 삭제")
    @DeleteMapping(path = "/{comment-id}")
    public void deleteComment(
            @PathVariable("comment-id") Long commentId
    ){
        commentService.deleteComment(commentId);
    }

    @Operation(summary = "코멘트 좋아요 등록")
    @PostMapping(path = "/{comment-id}/member/{member-id}/like/increase")
    public void increaseLike(
            @PathVariable("comment-id") Long commentId,
            @PathVariable("member-id") Long memberId
    ){
        commentService.increaseLike(commentId,memberId);
    }

    @Operation(summary = "코멘트 좋아요 삭제")
    @PostMapping(path = "/{comment-id}/member/{member-id}/like/decrease")
    public void decreaseLike(
            @PathVariable("comment-id") Long commentId,
            @PathVariable("member-id") Long memberId
    ){
        commentService.decreaseLike(commentId,memberId);
    }

    @Operation(summary = "좋아요 id로 삭제")
    @DeleteMapping(path = "/like/{like-id}")
    public void deleteLike(
            @PathVariable("like-id") Long likeId
    ){
        commentService.deleteLike(likeId);
    }

}
