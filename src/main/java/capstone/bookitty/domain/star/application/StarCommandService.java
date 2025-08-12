package capstone.bookitty.domain.star.application;

import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.exception.UnauthenticatedMemberException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.domain.star.api.dto.StarSaveRequest;
import capstone.bookitty.domain.star.api.dto.StarUpdateRequest;
import capstone.bookitty.domain.star.domain.Star;
import capstone.bookitty.domain.star.event.StarEventPublisher;
import capstone.bookitty.domain.star.exception.StarNotFoundException;
import capstone.bookitty.domain.star.repository.StarRepository;
import capstone.bookitty.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StarCommandService {

    private final StarRepository starRepository;
    private final MemberRepository memberRepository;
    private final StarEventPublisher starEventPublisher;
    private final ApplicationEventPublisher eventPublisher;

    @CacheEvict(value = "book-ratings", key = "#request.isbn()")
    public Long saveStar(StarSaveRequest request) {

        validateStarUniqueness(request.isbn(), request.memberId());

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new MemberNotFoundException(request.memberId()));

        Star star = Star.builder()
                .score(request.score())
                .isbn(request.isbn())
                .memberId(request.memberId())
                .build();

        Star savedStar = starRepository.save(star);

        // Publish star created event
        starEventPublisher.publishStarCreated(request.isbn(), request.memberId(), request.score());

        return savedStar.getId();

    }

    public void updateStar(Long starId, StarUpdateRequest request) {

        Star star = starRepository.findById(starId)
                .orElseThrow(() -> new StarNotFoundException(starId));

        Double previousScore = star.getScore();
        star.updateStar(request.score());

        // Publish star updated event
        starEventPublisher.publishStarUpdated(star.getIsbn(), star.getMemberId(), previousScore, request.score());

    }

    public void deleteStar(Long starId) {

        Star star = starRepository.findById(starId)
                .orElseThrow(() -> new StarNotFoundException(starId));

        Member member = memberRepository.findById(star.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException(star.getMemberId()));

        String email = SecurityUtil.getCurrentMemberEmail();
        if (email == null) throw new UnauthenticatedMemberException();
        Member current = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthenticatedMemberException(email));

        current.validatePermissionTo(member);

        // Publish star deleted event before deletion
        starEventPublisher.publishStarDeleted(star.getIsbn(), star.getMemberId(), star.getScore());

        starRepository.delete(star);

    }

    //==private methods==//

    private void validateStarUniqueness(String isbn, Long memberId) {

        if (starRepository.existsByIsbnAndMemberId(isbn, memberId)) {
            throw new IllegalArgumentException("Star rating already exists for this member and ISBN.");
        }

    }

}
