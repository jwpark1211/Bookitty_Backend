package capstone.bookitty.domain.star.api;

import capstone.bookitty.domain.star.api.dto.StarInfoResponse;
import capstone.bookitty.domain.star.application.StarQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Tag(name = "star", description = "평점 관리 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/stars")
public class StarQueryApi {

    private final StarQueryService starQueryService;

    @Operation(summary = "starId로 평점 가져오기")
    @GetMapping(path = "/{star-id}")
    public ResponseEntity<StarInfoResponse> getStarById(
            @PathVariable("star-id") Long starId
    ) {

        StarInfoResponse response = starQueryService.findStarByStarId(starId);
        return ResponseEntity.ok().body(response);

    }

    @Operation(summary = "memberId로 평점 가져오기")
    @GetMapping(path = "/member/{member-id}")
    public ResponseEntity<Page<StarInfoResponse>> getStarsByMemberId(
            @PathVariable("member-id") Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable
    ) {

        Page<StarInfoResponse> responseList = starQueryService.findByMemberId(memberId, pageable);
        return ResponseEntity.ok(responseList);

    }

    @Operation(summary = "isbn으로 평점 가져오기")
    @GetMapping(path = "/isbn/{isbn}")
    public ResponseEntity<Page<StarInfoResponse>> getStarsByIsbn(
            @PathVariable("isbn") String isbn,
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable
    ) {

        Page<StarInfoResponse> responseList = starQueryService.findByIsbn(isbn, pageable);
        return ResponseEntity.ok(responseList);

    }

    @Operation(summary = "전체 평점 가져오기")
    @GetMapping(path = "/all")
    public ResponseEntity<Page<StarInfoResponse>> getAllStars(
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable
    ) {

        Page<StarInfoResponse> responseList = starQueryService.findAll(pageable);
        return ResponseEntity.ok(responseList);

    }

}
