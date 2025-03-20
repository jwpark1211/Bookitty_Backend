package capstone.bookitty.domain.bookState.application;

import capstone.bookitty.domain.bookState.dto.*;
import capstone.bookitty.global.dto.IdResponse;
import capstone.bookitty.domain.bookState.domain.BookState;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.bookState.domain.State;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.bookState.exception.StateNotFoundException;
import capstone.bookitty.domain.bookState.repository.BookStateRepository;
import capstone.bookitty.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookStateService {

    private final BookStateRepository stateRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public IdResponse saveState(StateSaveRequest request) {
        State reqState = request.state();
        if (reqState == null) {
            throw new IllegalArgumentException("state is invalid");
        }

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new MemberNotFoundException(request.memberId()));

        BookState bookState = BookState.builder()
                .member(member)
                .state(reqState)
                .isbn(request.isbn())
                .bookTitle(request.bookTitle())
                .bookAuthor(request.bookAuthor())
                .bookImgUrl(request.bookImgUrl())
                .categoryName(request.categoryName())
                .build();

        if (reqState == State.READ_ALREADY) {
            bookState.readAtNow();
        }

        try {
            stateRepository.saveAndFlush(bookState);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("BookState already exists for this member and ISBN.");
        }

        return new IdResponse(bookState.getId());
    }


    public StateInfoResponse findStateByStateId(Long stateId) {
        return stateRepository.findById(stateId)
                .map(StateInfoResponse::from)
                .orElseThrow(() -> new StateNotFoundException(stateId));
    }

    public Page<StateInfoResponse> findStates(String isbn, Long memberId, Pageable pageable) {
        Page<BookState> bookStates = stateRepository.findByFilters(isbn, memberId, pageable);
        return bookStates.map(StateInfoResponse::from);
    }

    @Transactional
    public StateUpdateResponse updateState(Long stateId, StateUpdateRequest request) {
        BookState bookState = stateRepository.findById(stateId)
                .orElseThrow(() -> new StateNotFoundException(stateId));

        bookState.updateState(request.state());
        return StateUpdateResponse.from(bookState);
    }

    @Transactional
    public void deleteState(Long stateId) {
        BookState bookState = stateRepository.findById(stateId)
                .orElseThrow(() -> new StateNotFoundException(stateId));
        stateRepository.delete(bookState);
    }

    public MonthlyStaticsResponse findMonthlyStatByMemberId(Long memberId, int year) {
        List<BookState> statesM = stateRepository.findByMemberId(memberId);
        int[] monthly = new int[12];
        for(int i=0; i<12; i++){ monthly[i] = 0; }
        for(BookState state : statesM){
            if(state.getState()==State.READ_ALREADY){
                if(state.getReadAt()!=null && state.getReadAt().getYear()==year)
                    monthly[state.getReadAt().getMonthValue()-1]++;
            }
        }
        return new MonthlyStaticsResponse(monthly);
    }

    public CategoryStaticsResponse findCategoryStateByMemberId(Long memberId) {
        List<BookState> statesC = stateRepository.findByMemberId(memberId);
        int total = statesC.size();
        int literature = 0,humanities = 0, businessEconomics = 0, selfImprovement = 0,scienceTechnology = 0, etc = 0;
        for(BookState state: statesC) {
            if (state.getState() == State.READ_ALREADY) {
                if (state.getCategoryName().contains("경제경영") || state.getCategoryName().contains("경제/경영")) {
                    businessEconomics++;
                } else if (state.getCategoryName().contains("소설/시/희곡") || state.getCategoryName().contains("서양고전문학")
                        || state.getCategoryName().contains("동양고전문학")) {
                    literature++;
                } else if (state.getCategoryName().contains("인문학") || state.getCategoryName().contains("인문/사회")) {
                    humanities++;
                } else if (state.getCategoryName().contains("과학") || state.getCategoryName().contains("컴퓨터/모바일")
                        || state.getCategoryName().contains("컴퓨터") || state.getCategoryName().contains("자연과학")) {
                    scienceTechnology++;
                } else if (state.getCategoryName().contains("자기계발")) {
                    selfImprovement++;
                } else{
                    etc++;}
            }
        }
        return new CategoryStaticsResponse(literature,humanities,businessEconomics,
                selfImprovement,scienceTechnology,etc);
    }

}
