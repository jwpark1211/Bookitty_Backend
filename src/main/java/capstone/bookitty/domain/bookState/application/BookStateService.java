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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            stateRepository.save(bookState);
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
        Map<Integer, Long> monthCount = stateRepository.findByMemberId(memberId).stream()
                .filter(s -> s.getState() == State.READ_ALREADY)
                .map(BookState::getReadAt)
                .filter(Objects::nonNull)
                .filter(d -> d.getYear() == year)
                .collect(Collectors.groupingBy(d -> d.getMonthValue(), Collectors.counting()));

        int[] monthly = IntStream.rangeClosed(1, 12)
                .map(i -> monthCount.getOrDefault(i, 0L).intValue())
                .toArray();

        return new MonthlyStaticsResponse(monthly);
    }


    public CategoryStaticsResponse findCategoryStateByMemberId(Long memberId) {
        List<BookState> states = stateRepository.findByMemberId(memberId);

        Map<String, Long> grouped = states.stream()
                .filter(state -> state.getState() == State.READ_ALREADY)
                .map(BookState::getCategoryName)
                .map(this::mapToCategory)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return new CategoryStaticsResponse(
                grouped.getOrDefault("literature", 0L).intValue(),
                grouped.getOrDefault("humanities", 0L).intValue(),
                grouped.getOrDefault("businessEconomics", 0L).intValue(),
                grouped.getOrDefault("selfImprovement", 0L).intValue(),
                grouped.getOrDefault("scienceTechnology", 0L).intValue(),
                grouped.getOrDefault("etc", 0L).intValue()
        );
    }

    private String mapToCategory(String categoryName) {
        if (categoryName.contains("경제경영") || categoryName.contains("경제/경영")) {
            return "businessEconomics";
        } else if (categoryName.contains("소설/시/희곡") || categoryName.contains("서양고전문학")
                || categoryName.contains("동양고전문학")) {
            return "literature";
        } else if (categoryName.contains("인문학") || categoryName.contains("인문/사회")) {
            return "humanities";
        } else if (categoryName.contains("과학") || categoryName.contains("컴퓨터/모바일")
                || categoryName.contains("컴퓨터") || categoryName.contains("자연과학")) {
            return "scienceTechnology";
        } else if (categoryName.contains("자기계발")) {
            return "selfImprovement";
        } else {
            return "etc";
        }
    }

}
