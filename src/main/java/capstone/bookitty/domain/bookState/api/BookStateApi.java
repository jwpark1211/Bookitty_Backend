package capstone.bookitty.domain.bookState.api;

import capstone.bookitty.domain.bookState.dto.*;
import capstone.bookitty.global.dto.IdResponse;
import capstone.bookitty.domain.bookState.application.BookStateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Tag(name = "book state", description = "책 상태 관련 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/states")
public class BookStateApi {

    private final BookStateService bookStateService;

    @Operation(summary = "책 상태 생성 / state=READING or WANT_TO_READ or READ_ALREADY")
    @PostMapping
    public ResponseEntity<IdResponse> createBookState(
            @RequestBody @Valid StateSaveRequest request
    ){
        IdResponse response = bookStateService.saveState(request);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "책 상태 목록 조회 (ISBN, memberId 필터 가능, 페이지네이션 지원)")
    @GetMapping
    public ResponseEntity<Page<StateInfoResponse>> getBookStates(
            @RequestParam(name = "isbn", required = false) String isbn,
            @RequestParam(name = "memberId", required = false) Long memberId,
            @PageableDefault(size = 10 , sort = "createdAt", direction = DESC) Pageable pageable
    ) {
        Page<StateInfoResponse> responseList = bookStateService.findStates(isbn, memberId, pageable);
        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "stateId로 책 상태 가져오기")
    @GetMapping(path = "/{state-id}")
    public ResponseEntity<StateInfoResponse> getStateById(
            @PathVariable("state-id") Long stateId
    ){
        StateInfoResponse response = bookStateService.findStateByStateId(stateId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "state 정보 수정(READING, WANT_TO_READ, READ_ALREADY)")
    @PatchMapping(path = "/{state-id}")
    public ResponseEntity<StateUpdateResponse> updateState(
            @PathVariable("state-id") Long stateId,
            @RequestBody @Valid StateUpdateRequest request
    ){
        StateUpdateResponse response = bookStateService.updateState(stateId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "state 삭제")
    @DeleteMapping(path = "/{state-id}")
    public ResponseEntity<Void> deleteState(
            @PathVariable("state-id") Long stateId
    ){
        bookStateService.deleteState(stateId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "달별 책 개수 조회")
    @GetMapping("/statistics/monthly")
    public ResponseEntity<MonthlyStaticsResponse> getMonthlyStatistics(
            @RequestParam(name = "memberId") Long memberId,
            @RequestParam(name = "year") int year
    ) {
        MonthlyStaticsResponse response = bookStateService.findMonthlyStatByMemberId(memberId, year);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리별 책 개수 조회")
    @GetMapping("/statistics/category")
    public ResponseEntity<CategoryStaticsResponse> getCategoryStatistics(
            @RequestParam(name = "memberId") Long memberId
    ) {
        CategoryStaticsResponse response = bookStateService.findCategoryStateByMemberId(memberId);
        return ResponseEntity.ok(response);
    }
}
