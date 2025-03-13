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


@Tag(name="star", description = "평점 관리 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/stars")
public class StarApi {

    private final StarService starService;

    @Operation(summary = "평점 생성[평점은 0.5부터 5까지 0.5단위로 증가합니다.]")
    @PostMapping
    public ResponseEntity<IdResponse> createStar(
            @RequestBody @Valid StarSaveRequest request
    ){
        IdResponse response = starService.saveStar(request);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "starId로 평점 가져오기")
    @GetMapping(path="/{star-id}")
    public ResponseEntity<StarInfoResponse> getStarById(
            @PathVariable("star-id") Long starId
    ){
        StarInfoResponse response = starService.findStarByStarId(starId);
        return ResponseEntity.ok().body(response);
    }


    @Operation(summary = "평점 가져오기(전체, isbn, memberId 중 선택)")
    @GetMapping
    public ResponseEntity<Page<StarInfoResponse>> getStars(
            @RequestParam(name = "isbn", required = false) String isbn,
            @RequestParam(name = "memberId", required = false) Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable
    ) {
        Page<StarInfoResponse> responseList = starService.findStars(isbn, memberId, pageable);
        return ResponseEntity.ok(responseList);
    }


    @Operation(summary = "평점 수정")
    @PutMapping(path="/{star-id}")
    public ResponseEntity<Void> updateStar(
            @PathVariable("star-id") Long starId,
            @RequestBody @Valid StarUpdateRequest request
    ){
        starService.updateStar(starId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "평점 삭제")
    @DeleteMapping(path = "/{star-id}")
    public ResponseEntity<Void> deleteStar(
            @PathVariable("star-id") Long starId
    ){
        starService.deleteStar(starId);
        return ResponseEntity.noContent().build();
    }
}
