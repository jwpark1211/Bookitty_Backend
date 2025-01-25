package capstone.bookitty.domain.service;

import capstone.bookitty.domain.dto.commonDto.IdResponse;
import capstone.bookitty.domain.dto.starDto.StarInfoResponse;
import capstone.bookitty.domain.dto.starDto.StarSaveRequest;
import capstone.bookitty.domain.dto.starDto.StarUpdateRequest;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.entity.Star;
import capstone.bookitty.domain.exception.MemberNotFoundException;
import capstone.bookitty.domain.exception.StarNotFoundException;
import capstone.bookitty.domain.repository.MemberRepository;
import capstone.bookitty.domain.repository.StarRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StarService {

    private final StarRepository starRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public IdResponse saveStar(StarSaveRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(()->new MemberNotFoundException(request.memberId()));

        if(starRepository.existsByMemberIdAndIsbn(request.memberId(),request.isbn()))
            throw new IllegalArgumentException("star already exists.");

        Star star = Star.builder()
                .score(request.score())
                .isbn(request.isbn())
                .member(member)
                .build();

        starRepository.save(star);
        return new IdResponse(star.getId());
    }

    public StarInfoResponse findStarByStarId(Long starId) {
        return starRepository.findById(starId)
                .map(StarInfoResponse::from)
                .orElseThrow(()->new StarNotFoundException(starId));
    }

    public Page<StarInfoResponse> findStarByISBN(String isbn, Pageable pageable) {
        return starRepository.findByIsbn(isbn,pageable)
                .map(StarInfoResponse::from);
    }

    public Page<StarInfoResponse> findStarByMemberId(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new MemberNotFoundException(memberId));
        return starRepository.findByMemberId(memberId,pageable)
                .map(StarInfoResponse::from);
    }

    @Transactional
    public void updateStar(Long starId, StarUpdateRequest request) {
        try {
            Star star = starRepository.findById(starId)
                    .orElseThrow(() -> new StarNotFoundException(starId));
            star.updateStar(request.score());
        } catch (OptimisticLockException e) {
            throw new IllegalStateException("Concurrent update detected for Star with ID " + starId, e);
        }
    }

    @Transactional
    public void deleteStar(Long starId) {
        Star star = starRepository.findById(starId)
                .orElseThrow(()-> new StarNotFoundException(starId));
        starRepository.delete(star);
    }

    public Page<StarInfoResponse> findAllStar(Pageable pageable) {
        return starRepository.findAll(pageable)
                .map(StarInfoResponse::from);
    }

    public StarInfoResponse findStarByMemberIdAndIsbn(Long memberId, String isbn) {
        Star star = starRepository.findByMemberIdAndIsbn(memberId,isbn)
                .orElseThrow(()-> new EntityNotFoundException(
                        "Star with memberID:"+memberId+",Isbn:"+isbn+"not found."));
        return StarInfoResponse.from(star);
    }

}
