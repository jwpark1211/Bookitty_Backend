package capstone.bookitty.domain.controller;

import capstone.bookitty.domain.dto.ResponseType.BasicResponse;
import capstone.bookitty.domain.dto.ResponseType.ResponseCounter;
import capstone.bookitty.domain.dto.ResponseType.ResponseString;
import capstone.bookitty.domain.service.StarService;
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

import static capstone.bookitty.domain.dto.StarDTO.*;

@Tag(name="평점", description = "평점 관리 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/star")
public class StarController {

    private final StarService starService;

    @Operation(summary = "평점 생성[평점은 0.5부터 5까지 0.5단위로 증가합니다.]")
    @PostMapping(path = "/new")
    public ResponseEntity<? extends BasicResponse> saveStar(
            @RequestBody @Valid StarSaveRequest request
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<IdResponse>(
                     starService.saveStar(request)));
    }

    @Operation(summary = "starId로 평점 가져오기")
    @GetMapping(path="/{star-id}")
    public ResponseEntity<? extends BasicResponse> getStarById(
            @PathVariable("star-id") Long starId
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<StarInfoResponse>(
                        starService.findStarByStarId(starId)));
    }


    @Operation(summary = "전체 평점 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/all")
    public ResponseEntity<? extends BasicResponse> getAllStar(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<Page<StarInfoResponse>>(
                        starService.findAllStar(pageable)));
    }

    @Operation(summary = "isbn으로 평점 리스트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/isbn/{isbn}")
    public ResponseEntity<? extends BasicResponse> getStarByISBN(
            @PathVariable("isbn") String isbn,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<Page<StarInfoResponse>>(
                        starService.findStarByISBN(isbn,pageable)));
    }

    @Operation(summary = "member id로 평점 리스트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/member/{member-id}")
    public ResponseEntity<? extends BasicResponse> getStarByMemberId(
            @PathVariable("member-id") Long memberId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<Page<StarInfoResponse>>(
                        starService.findStarByMemberId(memberId,pageable)));
    }

    @Operation(summary = "member id와 isbn으로 평점 가져오기")
    @GetMapping(path = "/member/{member-id}/isbn/{isbn}")
    public ResponseEntity<? extends BasicResponse> getStarByIsbnAndMemberId(
            @PathVariable("isbn") String isbn,
            @PathVariable("member-id") Long memberId
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<StarInfoResponse>(
                        starService.findStarByMemberIdAndIsbn(memberId,isbn)));
    }

    @Operation(summary = "평점 수정")
    @PatchMapping(path="/{star-id}")
    public ResponseEntity<? extends BasicResponse> updateStar(
            @PathVariable("star-id") Long starId,
            @RequestBody @Valid StarUpdateRequest request
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<StarUpdateResponse>(starService.updateStar(starId,request)));
    }

    @Operation(summary = "평점 삭제")
    @DeleteMapping(path = "/{star-id}")
    public ResponseEntity<? extends BasicResponse> deleteStar(
            @PathVariable("star-id") Long starId
    ){
        starService.deleteStar(starId);
        return ResponseEntity.ok()
                .body(new ResponseString("delete Star!"));
    }
}
