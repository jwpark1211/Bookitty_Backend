package capstone.bookitty.domain.service;

import capstone.bookitty.domain.dto.bookStateDto.*;
import capstone.bookitty.domain.dto.commonDto.IdResponse;
import capstone.bookitty.domain.entity.BookState;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.entity.State;
import capstone.bookitty.domain.exception.MemberNotFoundException;
import capstone.bookitty.domain.exception.StateNotFoundException;
import capstone.bookitty.domain.repository.BookStateRepository;
import capstone.bookitty.domain.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

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
        if(reqState == null) throw new IllegalArgumentException("state is invalid");

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(()-> new MemberNotFoundException(request.memberId()));

        if(stateRepository.existsByMemberIdAndIsbn(request.memberId(), request.isbn()))
            throw new IllegalArgumentException("bookState already exists");

        BookState bookState = BookState.builder()
                .member(member)
                .state(reqState)
                .isbn(request.isbn())
                .bookTitle(request.bookTitle())
                .bookAuthor(request.bookAuthor())
                .bookImgUrl(request.bookImgUrl())
                .categoryName(request.categoryName())
                .build();

        if(reqState == State.READ_ALREADY) bookState.readAtNow();

        stateRepository.save(bookState);
        return new IdResponse(bookState.getId());
    }


    public Page<StateInfoResponse> findStateByISBN(String isbn, Pageable pageable) {
        return stateRepository.findByIsbn(isbn, pageable)
                .map(StateInfoResponse::from);
    }

    public StateInfoResponse findStateByMemberAndIsbn(String isbn, Long memberId){
        BookState state = stateRepository.findByMemberIdAndIsbn(memberId,isbn)
                .orElseThrow(()-> new MemberNotFoundException(memberId));
        return StateInfoResponse.from(state);
    }

    public StateInfoResponse findStateByStateId(Long stateId) {
        return stateRepository.findById(stateId)
                .map(StateInfoResponse::from)
                .orElseThrow(() -> new StateNotFoundException(stateId));
    }

    public Page<StateInfoResponse> findStateByMemberId(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new MemberNotFoundException(memberId));

        return stateRepository.findByMemberId(memberId,pageable)
                .map(StateInfoResponse::from);
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

    public List<StateInfoResponse> findAll() {
        return stateRepository.findAll().stream()
                .map(StateInfoResponse::from)
                .collect(Collectors.toList());
    }
}
