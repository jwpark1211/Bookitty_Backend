package capstone.bookitty.domain.bookState.api;

import capstone.bookitty.domain.bookState.dto.*;
import capstone.bookitty.domain.dto.*;
import capstone.bookitty.domain.dto.bookStateDto.*;
import capstone.bookitty.global.dto.IdResponse;
import capstone.bookitty.domain.bookState.application.BookStateService;
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

import java.util.List;

@Tag(name = "책 상태", description = "책 상태 관련 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/state")
public class BookStateApi {

    private final BookStateService bookStateService;

    @Operation(summary = "책 상태 생성 / state=READING or WANT_TO_READ or READ_ALREADY")
    @PostMapping(path = "/new")
    public ResponseEntity<IdResponse> saveBookState(
            @RequestBody @Valid StateSaveRequest request
    ){
        IdResponse response = bookStateService.saveState(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "isbn으로 책 상태 리스트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/isbn/{isbn}")
    public ResponseEntity<Page<StateInfoResponse>> getStateByISBN(
            @PathVariable("isbn") String isbn,
            @PageableDefault(sort = "id", size=10) Pageable pageable
    ){
        Page<StateInfoResponse> responseList = bookStateService.findStateByISBN(isbn, pageable);
        return ResponseEntity.ok().body(responseList);
    }

    @Operation(summary = "stateId로 책 상태 가져오기")
    @GetMapping(path = "/{state-id}")
    public ResponseEntity<StateInfoResponse> getStateById(
            @PathVariable("state-id") Long stateId
    ){
        StateInfoResponse response = bookStateService.findStateByStateId(stateId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "isbn과 memberId로 책 상태 가져오기")
    @GetMapping(path = "/isbn/{isbn}/member/{member-id}")
    public ResponseEntity<StateInfoResponse> getStateByMemberIdAndIsbn(
            @PathVariable("member-id") Long memberId,
            @PathVariable("isbn") String isbn
    ){
        StateInfoResponse response = bookStateService.findStateByMemberAndIsbn(isbn,memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "memberId로 책 상태 리스트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/member/{member-id}")
    public ResponseEntity<Page<StateInfoResponse>> getStateByMemberId(
            @PathVariable("member-id") Long memberId,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size=10) Pageable pageable
    ){
        Page<StateInfoResponse> responseList =
                bookStateService.findStateByMemberId(memberId, pageable);
        return ResponseEntity.ok().body(responseList);
    }

    @Operation(summary = "모든 bookState 확인")
    @GetMapping(path = "/all")
    public ResponseEntity<List<StateInfoResponse>> getAllState(){
        List<StateInfoResponse> responseList = bookStateService.findAll();
        return ResponseEntity.ok().body(responseList);
    }

    @Operation(summary = "state 정보 수정 / state=READING or WANT_TO_READ or READ_ALREADY")
    @PatchMapping(path = "/{state-id}")
    public ResponseEntity<StateUpdateResponse> updateState(
            @PathVariable("state-id") Long stateId,
            @RequestBody @Valid StateUpdateRequest request
    ){
        StateUpdateResponse response = bookStateService.updateState(stateId, request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "state 삭제")
    @DeleteMapping(path = "/{state-id}")
    public void deleteState(
            @PathVariable("state-id") Long stateId
    ){
        bookStateService.deleteState(stateId);
    }

    @Operation(summary = "달별 책 개수: year(연도 기입 -> ex. 2024)")
    @GetMapping(path = "/statics/members/{member-id}/month/{year}")
    public ResponseEntity<MonthlyStaticsResponse> StatWithMonthByMemberId(
            @PathVariable("member-id") Long memberId,
            @PathVariable("year") int year
    ){
        MonthlyStaticsResponse response = bookStateService.findMonthlyStatByMemberId(memberId,year);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "카테고리별 책 개수(문학,인문학,경영/경제,자기계발,컴퓨터/과학,그 외)")
    @GetMapping(path = "/statics/members/{member-id}/category")
    public ResponseEntity<CategoryStaticsResponse> statWithCategoryByMemberId(
            @PathVariable("member-id") Long memberId
    ){
        CategoryStaticsResponse response = bookStateService.findCategoryStateByMemberId(memberId);
        return ResponseEntity.ok().body(response);
    }
}
