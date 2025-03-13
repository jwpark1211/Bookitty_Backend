package capstone.bookitty.domain.star.application;

import capstone.bookitty.global.dto.IdResponse;
import capstone.bookitty.domain.star.dto.StarInfoResponse;
import capstone.bookitty.domain.star.dto.StarSaveRequest;
import capstone.bookitty.domain.star.dto.StarUpdateRequest;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.star.domain.Star;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.star.exception.StarNotFoundException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.domain.star.repository.StarRepository;
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

    public Page<StarInfoResponse> findStars(String isbn, Long memberId, Pageable pageable) {
        Page<Star> stars = starRepository.findByFilters(isbn, memberId, pageable);
        return stars.map(StarInfoResponse::from);
    }
}
