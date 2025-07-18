package capstone.bookitty.domain.star.application;

import capstone.bookitty.domain.star.api.dto.StarInfoResponse;
import capstone.bookitty.domain.star.domain.Star;
import capstone.bookitty.domain.star.exception.StarNotFoundException;
import capstone.bookitty.domain.star.repository.StarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StarQueryService {

    private final StarRepository starRepository;

    public StarInfoResponse findStarByStarId(Long starId) {
        return starRepository.findById(starId)
                .map(StarInfoResponse::from)
                .orElseThrow(() -> new StarNotFoundException(starId));
    }

    public Page<StarInfoResponse> findByMemberId(Long memberId, Pageable pageable) {
        Page<Star> stars = starRepository.findByMemberId(memberId, pageable);
        return stars.map(StarInfoResponse::from);
    }

    public Page<StarInfoResponse> findByIsbn(String isbn, Pageable pageable) {
        Page<Star> stars = starRepository.findByIsbn(isbn, pageable);
        return stars.map(StarInfoResponse::from);
    }

    public Page<StarInfoResponse> findAll(Pageable pageable) {
        Page<Star> stars = starRepository.findAll(pageable);
        return stars.map(StarInfoResponse::from);
    }

}
