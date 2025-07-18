package capstone.bookitty.domain.star.api;

import capstone.bookitty.domain.star.api.dto.StarSaveRequest;
import capstone.bookitty.domain.star.api.dto.StarUpdateRequest;
import capstone.bookitty.domain.star.application.StarCommandService;
import capstone.bookitty.global.dto.IdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "star", description = "평점 관리 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/stars")
public class StarCommandApi {

    private final StarCommandService starCommandService;

    @Operation(summary = "평점 생성 [평점은 0.5부터 5까지 0.5단위로 증가합니다.]")
    @PostMapping
    public ResponseEntity<IdResponse> createStar(
            @RequestBody @Valid StarSaveRequest request
    ) {

        IdResponse response = new IdResponse(starCommandService.saveStar(request));
        return ResponseEntity.status(201).body(response);

    }

    @Operation(summary = "평점 수정")
    @PutMapping(path = "/{star-id}")
    public ResponseEntity<Void> updateStar(
            @PathVariable("star-id") Long starId,
            @RequestBody @Valid StarUpdateRequest request
    ) {

        starCommandService.updateStar(starId, request);
        return ResponseEntity.noContent().build();

    }

    @Operation(summary = "평점 삭제")
    @DeleteMapping(path = "/{star-id}")
    public ResponseEntity<Void> deleteStar(
            @PathVariable("star-id") Long starId
    ) {

        starCommandService.deleteStar(starId);
        return ResponseEntity.noContent().build();

    }

}
