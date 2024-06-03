package capstone.bookitty.domain.controller;

import capstone.bookitty.domain.dto.ResponseType.BasicResponse;
import capstone.bookitty.domain.dto.ResponseType.ResponseCounter;
import capstone.bookitty.domain.dto.ResponseType.ResponseString;
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

import static capstone.bookitty.domain.dto.CommentDTO.*;

@Tag(name="코멘트", description = "코멘트 관리 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "코멘트 생성 / content는 최소 1자, 최대 100자")
    @PostMapping(path = "/new")
    public ResponseEntity<? extends BasicResponse> saveComment(
            @RequestBody @Valid CommentSaveRequest request
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<IdResponse>(
                   commentService.saveComment(request)));
    }

    @Operation(summary = "commentId로 코멘트 가져오기")
    @GetMapping(path="/{comment-id}")
    public ResponseEntity<? extends BasicResponse> getCommentById(
            @PathVariable("comment-id") Long commentId
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<CommentInfoResponse>(
                        commentService.findCommentByCommentId(commentId)));
    }

    @Operation(summary = "전체 코멘트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
                        "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/all")
    public ResponseEntity<? extends BasicResponse> getAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<Page<CommentInfoResponse>>(
                   commentService.findAllComment(pageable)));
    }

    @Operation(summary = "isbn으로 코멘트 리스트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/isbn/{isbn}")
    public ResponseEntity<? extends BasicResponse> getCommentByISBN(
            @PathVariable("isbn") String isbn,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<Page<CommentInfoResponse>>(
                        commentService.findCommentByIsbn(isbn, pageable)));
    }

    @Operation(summary = "memberId로 코멘트 리스트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/member/{member-id}")
    public ResponseEntity<? extends BasicResponse> getCommentByMemberId(
            @PathVariable("member-id") Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<Page<CommentInfoResponse>>(
                        commentService.findCommentByMemberId(memberId, pageable)));
    }

    @Operation(summary = "코멘트 수정 / content는 최소 1자, 최대 100자")
    @PatchMapping(path = "/{comment-id}")
    public ResponseEntity<? extends BasicResponse> updateComment(
            @PathVariable("comment-id") Long commentId,
            @RequestBody @Valid CommentUpdateRequest request
    ){
        commentService.updateComment(commentId, request);
        return ResponseEntity.ok()
                .body(new ResponseCounter<CommentUpdateResponse>(
                        commentService.updateComment(commentId,request)));
    }

    @Operation(summary = "코멘트 삭제")
    @DeleteMapping(path = "/{comment-id}")
    public ResponseEntity<? extends BasicResponse> deleteComment(
            @PathVariable("comment-id") Long commentId
    ){
        commentService.deleteComment(commentId);
        return ResponseEntity.ok()
                .body(new ResponseString("delete Comment!"));
    }

    @Operation(summary = "코멘트 좋아요 등록")
    @PostMapping(path = "/{comment-id}/member/{member-id}/like/increase")
    public ResponseEntity<? extends BasicResponse> increaseLike(
            @PathVariable("comment-id") Long commentId,
            @PathVariable("member-id") Long memberId
    ){
        commentService.increaseLike(commentId,memberId);
        return ResponseEntity.ok()
                .body(new ResponseString("increase Like!"));
    }

    @Operation(summary = "코멘트 좋아요 삭제")
    @PostMapping(path = "/{comment-id}/member/{member-id}/like/decrease")
    public ResponseEntity<? extends BasicResponse> decreaseLike(
            @PathVariable("comment-id") Long commentId,
            @PathVariable("member-id") Long memberId
    ){
        commentService.decreaseLike(commentId,memberId);
        return ResponseEntity.ok()
                .body(new ResponseString("decrease Like!"));
    }

    @Operation(summary = "좋아요 id로 삭제")
    @DeleteMapping(path = "/like/{like-id}")
    public ResponseEntity<? extends BasicResponse> deleteLike(
            @PathVariable("like-id") Long likeId
    ){
        commentService.deleteLike(likeId);
        return ResponseEntity.ok()
                .body(new ResponseString("delete Like!"));
    }

}
