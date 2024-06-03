package capstone.bookitty.domain.service;

import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.entity.Star;
import capstone.bookitty.domain.repository.MemberRepository;
import capstone.bookitty.domain.repository.StarRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


import static capstone.bookitty.domain.dto.StarDTO.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StarService {

    private final StarRepository starRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public IdResponse saveStar(StarSaveRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(()->new EntityNotFoundException(
                        "Member with ID "+request.getMemberId()+" not found."));

        if(starRepository.existsByMemberIdAndIsbn(request.getMemberId(),request.getIsbn()))
            throw new IllegalArgumentException("star already exists.");

        Star star = Star.builder()
                .score(request.getScore())
                .isbn(request.getIsbn())
                .member(member)
                .build();

        starRepository.save(star);
        return new IdResponse(star.getId());
    }

    public StarInfoResponse findStarByStarId(Long starId) {
        return starRepository.findById(starId)
                .map(StarInfoResponse::of)
                .orElseThrow(()->new EntityNotFoundException("Star with ID "+starId+" not found."));
    }

    public Page<StarInfoResponse> findStarByISBN(String isbn, Pageable pageable) {
        return starRepository.findByIsbn(isbn,pageable)
                .map(StarInfoResponse::of);
    }

    public Page<StarInfoResponse> findStarByMemberId(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new EntityNotFoundException("Member with ID "+ memberId+" not found."));
        return starRepository.findByMemberId(memberId,pageable)
                .map(StarInfoResponse::of);
    }

    @Transactional
    public StarUpdateResponse updateStar(Long starId, StarUpdateRequest request) {
        Star star = starRepository.findById(starId)
                .orElseThrow(()-> new EntityNotFoundException("Star with ID "+starId+" not found."));
        star.updateStar(request.getScore());
        return StarUpdateResponse.of(star);
    }

    @Transactional
    public void deleteStar(Long starId) {
        Star star = starRepository.findById(starId)
                .orElseThrow(()-> new EntityNotFoundException("Star with ID "+starId+" not found."));
        starRepository.delete(star);
    }

    public Page<StarInfoResponse> findAllStar(Pageable pageable) {
        return starRepository.findAll(pageable)
                .map(StarInfoResponse::of);
    }
}
