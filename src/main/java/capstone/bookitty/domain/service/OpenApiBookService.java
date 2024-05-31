package capstone.bookitty.domain.service;

import capstone.bookitty.domain.entity.Gender;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.repository.MemberRepository;
import capstone.bookitty.global.api.dto.AladinBestSellerResponseDTO;
import capstone.bookitty.global.api.dto.AladinBookListResponseDTO;
import capstone.bookitty.global.api.dto.AladinBookSearchResponseDTO;
import capstone.bookitty.global.api.dto.NaruPopularBookListDto;
import capstone.bookitty.global.api.openApi.AladinOpenApi;
import capstone.bookitty.global.api.openApi.NaruOpenApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class OpenApiBookService {

    private final AladinOpenApi aladinOpenApi;
    private final NaruOpenApi naruOpenApi;
    private final MemberRepository memberRepository;

    public Mono<AladinBookListResponseDTO> searchByBookISBN(String isbn) {
        return aladinOpenApi.searchByBookISBN(isbn);
    }

    public Mono<AladinBookSearchResponseDTO> searchByKeyword(String keyword) {
        return aladinOpenApi.searchByKeyword(keyword);
    }

    @Cacheable(value = "bestsellers", cacheManager = "cacheManager")
    public Mono<AladinBestSellerResponseDTO> getBestSeller() {
        return aladinOpenApi.getAllBestSeller();
    }

    @Cacheable(value = "bestsellersByGenre", key = "#a0",unless="#result == null")
    public Mono<AladinBestSellerResponseDTO> getBestSellerByGenre(int cid) {
        return aladinOpenApi.getBestSellerByGenre(cid);
    }

    @Cacheable(value = "newBooks", cacheManager = "cacheManager")
    public Mono<AladinBestSellerResponseDTO> getBestSellerNewBook() {
        return aladinOpenApi.getNewBook();
    }

    @Cacheable(value = "blogChoices", cacheManager = "cacheManager")
    public Mono<AladinBestSellerResponseDTO> getBlogChoice() {
        return aladinOpenApi.getBlogChoice();
    }

    @Cacheable(value = "genderAndAge", key = "#a0",unless="#result == null")
    public NaruPopularBookListDto getGenderAndAgeRecommendation(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found for ID: " + memberId));

        int gender = (member.getGender() == Gender.MALE) ? 1 : 2;
        int age = Period.between(member.getBirthDate(), LocalDate.now()).getYears();
        age = (age / 10) * 10;

        return naruOpenApi.getPopularBook(gender, age);
    }
}
