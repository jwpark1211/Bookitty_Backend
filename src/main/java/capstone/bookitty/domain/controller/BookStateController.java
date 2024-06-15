package capstone.bookitty.domain.controller;

import capstone.bookitty.domain.dto.ResponseType.BasicResponse;
import capstone.bookitty.domain.dto.ResponseType.ResponseCounter;
import capstone.bookitty.domain.dto.ResponseType.ResponseString;
import capstone.bookitty.domain.service.BookStateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static capstone.bookitty.domain.dto.BookStateDTO.*;

@Tag(name = "책 상태", description = "책 상태 관련 api 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/state")
public class BookStateController {

    private final BookStateService bookStateService;

    @Operation(summary = "책 상태 생성 / state=READING or WANT_TO_READ or READ_ALREADY")
    @PostMapping(path = "/new")
    public ResponseEntity<? extends BasicResponse> saveBookState(
            @RequestBody @Valid StateSaveRequest request
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<IdResponse>(
                        bookStateService.saveState(request)));
    }

    @Operation(summary = "isbn으로 책 상태 리스트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/isbn/{isbn}")
    public ResponseEntity<? extends BasicResponse> getStateByISBN(
            @PathVariable("isbn") String isbn,
            @PageableDefault(sort = "id", size=10) Pageable pageable
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<Page<StateInfoResponse>>(
                        bookStateService.findStateByISBN(isbn, pageable)));
    }

    @Operation(summary = "stateId로 책 상태 가져오기")
    @GetMapping(path = "/{state-id}")
    public ResponseEntity<? extends BasicResponse> getStateById(
            @PathVariable("state-id") Long stateId
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<StateInfoResponse>(
                        bookStateService.findStateByStateId(stateId)));
    }

    @Operation(summary = "isbn과 memberId로 책 상태 가져오기")
    @GetMapping(path = "/isbn/{isbn}/member/{member-id}")
    public ResponseEntity<? extends BasicResponse> getStateByMemberIdAndIsbn(
            @PathVariable("member-id") Long memberId,
            @PathVariable("isbn") String isbn
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<StateInfoResponse>(
                        bookStateService.findStateByMemberAndIsbn(isbn,memberId)));
    }

    @Operation(summary = "memberId로 책 상태 리스트 가져오기 / page는 requestParam으로 요청할 수 있습니다. / "+
            "size(한 페이지 당 element 수, default = 10), page(요청하는 페이지, 0부터 시작)")
    @GetMapping(path = "/member/{member-id}")
    public ResponseEntity<? extends BasicResponse> getStateByMemberId(
            @PathVariable("member-id") Long memberId,
            @PageableDefault(sort = "id", size=10) Pageable pageable
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<Page<StateInfoResponse>>(
                        bookStateService.findStateByMemberId(memberId,pageable)));
    }

    @Operation(summary = "모든 bookState 확인")
    @GetMapping(path = "/all")
    public ResponseEntity<? extends BasicResponse> getAllState(){
        return ResponseEntity.ok()
                .body(new ResponseCounter<List<StateInfoResponse>>(
                        bookStateService.findAll()));
    }

    @Operation(summary = "state 정보 수정 / state=READING or WANT_TO_READ or READ_ALREADY")
    @PatchMapping(path = "/{state-id}")
    public ResponseEntity<? extends BasicResponse> updateState(
            @PathVariable("state-id") Long stateId,
            @RequestBody @Valid StateUpdateRequest request
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<StateUpdateResponse>(
                        bookStateService.updateState(stateId,request)));
    }


    @Operation(summary = "state 삭제")
    @DeleteMapping(path = "/{state-id}")
    public ResponseEntity<? extends BasicResponse> deleteState(
            @PathVariable("state-id") Long stateId
    ){
        bookStateService.deleteState(stateId);
        return ResponseEntity.ok()
                .body(new ResponseString("delete state!"));
    }

    @Operation(summary = "달별 책 개수: year(연도 기입 -> ex. 2024)")
    @GetMapping(path = "/statics/members/{member-id}/month/{year}")
    public ResponseEntity<? extends BasicResponse> StatWithMonthByMemberId(
            @PathVariable("member-id") Long memberId,
            @PathVariable("year") int year
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<MonthlyStaticsResponse>(
                        bookStateService.findMonthlyStatByMemberId(memberId,year)));
    }

    @Operation(summary = "카테고리별 책 개수(문학,인문학,경영/경제,자기계발,컴퓨터/과학,그 외)")
    @GetMapping(path = "/statics/members/{member-id}/category")
    public ResponseEntity<? extends BasicResponse> statWithCategoryByMemberId(
            @PathVariable("member-id") Long memberId
    ){
        return ResponseEntity.ok()
                .body(new ResponseCounter<CategoryStaticsResponse>(
                        bookStateService.findCategoryStateByMemberId(memberId)));
    }

}
