package capstone.bookitty.domain.service;

import capstone.bookitty.domain.entity.Gender;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.repository.BookStateRepository;
import capstone.bookitty.domain.repository.MemberRepository;
import capstone.bookitty.domain.repository.StarRepository;
import capstone.bookitty.global.api.dto.*;
import capstone.bookitty.global.api.openApi.AladinOpenApi;
import capstone.bookitty.global.api.openApi.NaruOpenApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenApiBookService {

    private final AladinOpenApi aladinOpenApi;
    private final NaruOpenApi naruOpenApi;
    private final MemberRepository memberRepository;
    private final StarRepository starRepository;
    private final BookStateRepository bookStateRepository;

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

    @Cacheable(value = "bookRecommendations", key = "#a0",unless="#result == null")
    public NaruPopularBookListDto getTop10ForMember(long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));

        Gender gender = member.getGender();
        LocalDate birthDate = member.getBirthDate();
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        String ageGroup = determineAgeGroup(age);

        List<String> top10IsbnList = getTop10IsbnForMember(gender, ageGroup);
        List<NaruPopularBookDto> bookInfoList = top10IsbnList.stream()
                .map(this::searchByBookISBN)
                .map(mono -> mono.block())  // Mono를 block하여 동기적으로 결과를 가져오기
                .map(this::convertToNaruPopularBookDto)
                .collect(Collectors.toList());

        // 각 책의 랭킹을 설정
        for (int i = 0; i < bookInfoList.size(); i++) {
            bookInfoList.get(i).getDoc().setRanking(i + 1);
        }

        NaruPopularBookListDto responseWrapper = new NaruPopularBookListDto();
        NaruPopularBookListDto.PopularBookListResponse response = new NaruPopularBookListDto.PopularBookListResponse();
        response.setResultNum(bookInfoList.size());
        response.setDocs(bookInfoList);
        responseWrapper.setResponse(response);

        return responseWrapper;
    }

    private String determineAgeGroup(int age) {
        if (age <= 20) {
            return "TEEN";
        } else if (age <= 40) {
            return "ADULT";
        } else {
            return "MIDDLE_AGED";
        }
    }

    private NaruPopularBookDto convertToNaruPopularBookDto(AladinBookListResponseDTO aladinBook) {
        NaruPopularBookDto dto = new NaruPopularBookDto();
        NaruPopularBookDto.PopularBook popularBook = new NaruPopularBookDto.PopularBook();

        AladinBookListResponseDTO.DetailBook detailBook = aladinBook.getItem().get(0);  // 첫 번째 책 정보 사용
        popularBook.setBookname(detailBook.getTitle());
        popularBook.setAuthors(detailBook.getAuthor());
        popularBook.setPublisher(detailBook.getPublisher());
        popularBook.setIsbn13(detailBook.getIsbn13());
        popularBook.setBookImageURL(detailBook.getCover());

        dto.setDoc(popularBook);
        return dto;
    }

    private List<String> getTop10IsbnForMember(Gender gender, String ageGroup) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (ageGroup) {
            case "TEEN":
                startDate = endDate.minusYears(20);
                break;
            case "ADULT":
                startDate = endDate.minusYears(40);
                break;
            case "MIDDLE_AGED":
                startDate = endDate.minusYears(60);
                break;
            default:
                throw new IllegalArgumentException("Invalid age group");
        }

        List<Object[]> totalScores = starRepository.findTotalScoreByGenderAndBirthDate(gender, startDate, endDate);
        List<Object[]> stateCounts = bookStateRepository.findStateCountByGenderAndBirthDate(gender, startDate, endDate);

        Map<String, Double> isbnScores = new HashMap<>();

        for (Object[] scoreData : totalScores) {
            String isbn = (String) scoreData[0];
            Double score = (Double) scoreData[1];
            isbnScores.put(isbn, score);
        }

        for (Object[] stateData : stateCounts) {
            String isbn = (String) stateData[0];
            Long count = (Long) stateData[1];
            isbnScores.merge(isbn, count * 5.0, Double::sum);
        }

        return isbnScores.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
