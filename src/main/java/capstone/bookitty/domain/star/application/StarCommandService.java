package capstone.bookitty.domain.star.application;

import capstone.bookitty.domain.bookSimilarity.domain.RatingEvent;
import capstone.bookitty.domain.bookSimilarity.repository.RatingEventRepository;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.exception.MemberNotFoundException;
import capstone.bookitty.domain.member.exception.UnauthenticatedMemberException;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.domain.star.api.dto.StarSaveRequest;
import capstone.bookitty.domain.star.api.dto.StarUpdateRequest;
import capstone.bookitty.domain.star.domain.Star;
import capstone.bookitty.domain.star.exception.StarNotFoundException;
import capstone.bookitty.domain.star.repository.StarRepository;
import capstone.bookitty.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StarCommandService {

    private final StarRepository starRepository;
    private final MemberRepository memberRepository;
    private final RatingEventRepository ratingEventRepository;

    public Long saveStar(StarSaveRequest request) {

        validateStarUniqueness(request.isbn(), request.memberId());

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new MemberNotFoundException(request.memberId()));

        Star star = Star.builder()
                .score(request.score())
                .isbn(request.isbn())
                .memberId(request.memberId())
                .build();

        ratingEventRepository.save(new RatingEvent(request.memberId(), request.isbn()));
        return star.getId();

    }

    public void updateStar(Long starId, StarUpdateRequest request) {

        Star star = starRepository.findById(starId)
                .orElseThrow(() -> new StarNotFoundException(starId));
        star.updateStar(request.score());
        ratingEventRepository.save(new RatingEvent(star.getMemberId(), star.getIsbn()));

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

        starRepository.delete(star);

    }

    //==private methods==//

    private void validateStarUniqueness(String isbn, Long memberId) {

        if (!starRepository.existsByIsbnAndMemberId(isbn, memberId)) {
            throw new IllegalArgumentException("Star rating already exists for this member and ISBN.");
        }

    }

}
