package capstone.bookitty.domain.star.api;

import capstone.bookitty.global.dto.IdResponse;
import capstone.bookitty.domain.star.dto.StarInfoResponse;
import capstone.bookitty.domain.star.dto.StarSaveRequest;
import capstone.bookitty.domain.star.dto.StarUpdateRequest;
import capstone.bookitty.domain.star.application.StarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.*;


@Tag(name="평점", description = "평점 관리 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/star")
public class StarApi {

    private final StarService starService;

    @Operation(summary = "평점 생성[평점은 0.5부터 5까지 0.5단위로 증가합니다.]")
    @PostMapping(path = "/new")
    public ResponseEntity<IdResponse> saveStar(
            @RequestBody @Valid StarSaveRequest request
    ){
        IdResponse response = starService.saveStar(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "starId로 평점 가져오기")
    @GetMapping(path="/{star-id}")
    public ResponseEntity<StarInfoResponse> getStarById(
            @PathVariable("star-id") Long starId
    ){
        StarInfoResponse response = starService.findStarByStarId(starId);
        return ResponseEntity.ok().body(response);
    }


    @Operation(summary = "전체 평점 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/all")
    public ResponseEntity<Page<StarInfoResponse>> getAllStar(
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable
    ){
        Page<StarInfoResponse> responseList = starService.findAllStar(pageable);
        return ResponseEntity.ok().body(responseList);
    }

    @Operation(summary = "isbn으로 평점 리스트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/isbn/{isbn}")
    public ResponseEntity<Page<StarInfoResponse>> getStarByISBN(
            @PathVariable("isbn") String isbn,
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable
    ){
        Page<StarInfoResponse> responseList = starService.findStarByISBN(isbn, pageable);
        return ResponseEntity.ok().body(responseList);
    }

    @Operation(summary = "member id로 평점 리스트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/member/{member-id}")
    public ResponseEntity<Page<StarInfoResponse>> getStarByMemberId(
            @PathVariable("member-id") Long memberId,
            @PageableDefault(sort = "createdAt", direction = DESC) Pageable pageable
    ){
        Page<StarInfoResponse> responseList = starService.findStarByMemberId(memberId, pageable);
        return ResponseEntity.ok().body(responseList);
    }

    @Operation(summary = "member id와 isbn으로 평점 가져오기")
    @GetMapping(path = "/member/{member-id}/isbn/{isbn}")
    public ResponseEntity<StarInfoResponse> getStarByIsbnAndMemberId(
            @PathVariable("isbn") String isbn,
            @PathVariable("member-id") Long memberId
    ){
        StarInfoResponse response = starService.findStarByMemberIdAndIsbn(memberId, isbn);
        return ResponseEntity.ok().body(response);
    }
    @Operation(summary = "평점 수정")
    @PatchMapping(path="/{star-id}")
    public void updateStar(
            @PathVariable("star-id") Long starId,
            @RequestBody @Valid StarUpdateRequest request
    ){
        starService.updateStar(starId, request);
    }

    @Operation(summary = "평점 삭제")
    @DeleteMapping(path = "/{star-id}")
    public void deleteStar(
            @PathVariable("star-id") Long starId
    ){
        starService.deleteStar(starId);
    }
}
