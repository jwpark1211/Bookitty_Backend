package capstone.bookitty.domain.service;

import capstone.bookitty.domain.entity.BookState;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.entity.State;
import capstone.bookitty.domain.repository.BookStateRepository;
import capstone.bookitty.domain.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static capstone.bookitty.domain.dto.BookStateDTO.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookStateService {

    private final BookStateRepository stateRepository;
    private final MemberRepository memberRepository;
    @Transactional
    public IdResponse saveState(StateSaveRequest request) {

        State reqState;
        try {
            reqState = request.getState();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid state: " + request.getState(), e);
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(EntityNotFoundException::new);

        if(stateRepository.existsByMemberIdAndIsbn(request.getMemberId(), request.getIsbn()))
            throw new IllegalArgumentException("bookState already exists");

        BookState bookState = BookState.builder()
                .member(member)
                .state(reqState)
                .isbn(request.getIsbn())
                .bookTitle(request.getBookTitle())
                .bookAuthor(request.getBookAuthor())
                .bookImgUrl(request.getBookImgUrl())
                .categoryName(request.getCategoryName())
                .build();

        if(reqState == State.READ_ALREADY) bookState.readAtNow();

        stateRepository.save(bookState);
        return new IdResponse(bookState.getId());
    }


    public Page<StateInfoResponse> findStateByISBN(String isbn, Pageable pageable) {
        return stateRepository.findByIsbn(isbn, pageable)
                .map(StateInfoResponse::of);
    }

    public StateInfoResponse findStateByStateId(Long stateId) {
        return stateRepository.findById(stateId)
                .map(StateInfoResponse::of)
                .orElseThrow(() -> new EntityNotFoundException("BookState with ID " + stateId + " not found."));
    }

    public Page<StateInfoResponse> findStateByMemberId(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new EntityNotFoundException(
                        "Member with ID: "+memberId+" not found."));

        return stateRepository.findByMemberId(memberId,pageable)
                .map(StateInfoResponse::of);
    }

    @Transactional
    public StateUpdateResponse updateState(Long stateId, StateUpdateRequest request) {
        BookState bookState = stateRepository.findById(stateId)
                .orElseThrow(() -> new EntityNotFoundException("BookState not found for ID: " + stateId));

        bookState.updateState(request.getState());
        return StateUpdateResponse.of(bookState);
    }

    @Transactional
    public void deleteState(Long stateId) {
        BookState bookState = stateRepository.findById(stateId)
                .orElseThrow(() -> new EntityNotFoundException("BookState not found for ID: " + stateId));
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
                .map(StateInfoResponse::of)
                .collect(Collectors.toList());
    }
}
