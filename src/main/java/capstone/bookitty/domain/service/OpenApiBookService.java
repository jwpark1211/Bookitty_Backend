package capstone.bookitty.domain.service;

import capstone.bookitty.domain.entity.Gender;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.entity.Star;
import capstone.bookitty.domain.repository.BookStateRepository;
import capstone.bookitty.domain.repository.MemberRepository;
import capstone.bookitty.domain.repository.StarRepository;
import capstone.bookitty.global.api.dto.aladin.AladinBestSellerListResponse;
import capstone.bookitty.global.api.dto.aladin.AladinBookSearchListResponse;
import capstone.bookitty.global.api.openApi.AladinOpenApi;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenApiBookService {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiBookService.class);

    private final AladinOpenApi aladinOpenApi;
    private final MemberRepository memberRepository;
    private final StarRepository starRepository;
    private final BookStateRepository bookStateRepository;

    public Mono<AladinBookSearchListResponse> searchByBookISBN(String isbn) {
        return aladinOpenApi.searchByBookISBN(isbn);
    }

    public Mono<AladinBookSearchListResponse> searchByKeyword(String keyword) {
        return aladinOpenApi.searchByKeyword(keyword);
    }

    @Cacheable(value = "bestsellers", cacheManager = "cacheManager")
    public Mono<AladinBestSellerListResponse> getBestSeller() {
        return aladinOpenApi.getAllBestSeller();
    }

    @Cacheable(value = "bestsellersByGenre", key = "#a0", unless = "#result == null")
    public Mono<AladinBestSellerListResponse> getBestSellerByGenre(int cid) {
        return aladinOpenApi.getBestSellerByGenre(cid);
    }

    @Cacheable(value = "newBooks", cacheManager = "cacheManager")
    public Mono<AladinBestSellerListResponse> getBestSellerNewBook() {
        return aladinOpenApi.getNewBook();
    }

    @Cacheable(value = "blogChoices", cacheManager = "cacheManager")
    public Mono<AladinBestSellerListResponse> getBlogChoice() {
        return aladinOpenApi.getBlogChoice();
    }

    //@Cacheable(value = "bookRecommendations", key = "#a0", unless = "#result == null")
    public List<AladinBookSearchListResponse> getTop10ForMember(long memberId) {
        //parameter로 입력된 회원 id 정보를 통해 유효한 회원인지 판단합니다.
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));
        //회원의 정보(성별, 생년월일)을 수집하여 연령대(10대,20대 등) 정보를 업데이트합니다.
        Gender gender = member.getGender();
        LocalDate birthDate = member.getBirthDate();
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        //만약 신규 유저라 별점 정보가 0개인 경우 DefaultRecommendations(성별, 연령대)을 제공합니다.
        if(starRepository.findByMemberId(memberId).size() == 0)
            return getDefaultRecommendations(gender, age);
        //전체 유저의 별점 코사인 유사도 정보와 회원이 매긴 별점들 정보를 토대로 상위 10권의 책을 추천합니다.
        List<String> top10IsbnList = getRecommendations(memberId);
        List<AladinBookSearchListResponse> bookInfoList = top10IsbnList.stream()
                .map(this::searchByBookISBN)
                .map(mono -> mono.block())
                .collect(Collectors.toList());

        return bookInfoList;
    }

    private List<AladinBookSearchListResponse> getDefaultRecommendations(Gender gender, int age) {
        int years;
        if (age <= 20)  years = 20;
        else if (age <= 40) years = 40;
        else years = 60;

        List<Object[]> books = starRepository.findTotalScoreByGenderAndBirthDate(
                gender, LocalDate.now().minusYears(years), LocalDate.now());

        List<String> popularBooks = books.stream()
                .sorted((b1, b2) -> ((Double) b2[1]).compareTo((Double) b1[1]))
                .limit(10)
                .map(b -> (String) b[0])
                .collect(Collectors.toList());

        return popularBooks.stream()
                .map(this::searchByBookISBN)
                .map(mono -> mono.block())
                .collect(Collectors.toList());
    }

    private List<String> getRecommendations(long userId) {
        Map<Long, Map<String, Double>> userItemMatrix = new HashMap<>();
        List<Star> allStars = starRepository.findAll();
        for (Star star : allStars) {
            userItemMatrix
                    .computeIfAbsent(star.getMember().getId(), k -> new HashMap<>())
                    .put(star.getIsbn(), star.getScore());
        }

        Map<String, Double> userRatings = userItemMatrix.get(userId);
        Map<String, Double> recommendations = new HashMap<>();

        Set<String> ratedBooks = userRatings.keySet();

        for (Map.Entry<String, Double> entry : userRatings.entrySet()) {
            String ratedIsbn = entry.getKey();
            double ratingScore = entry.getValue();

            Map<String, Double> similarItems = calculateItemSimilarities(userItemMatrix, ratedIsbn);
            for (Map.Entry<String, Double> simEntry : similarItems.entrySet()) {
                String isbn = simEntry.getKey();
                double similarity = simEntry.getValue();
                if (!ratedBooks.contains(isbn)) {
                    recommendations.merge(isbn, ratingScore * similarity, Double::sum);
                }
            }
        }

        return recommendations.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(10)
                .collect(Collectors.toList());
    }

    private Map<String, Double> calculateItemSimilarities(Map<Long, Map<String, Double>> matrix, String targetIsbn) {
        Map<String, Double> similarities = new HashMap<>();

        Set<String> allIsbns = matrix.values().stream()
                .flatMap(ratings -> ratings.keySet().stream())
                .collect(Collectors.toSet());

        for (String isbn : allIsbns) {
            if (!isbn.equals(targetIsbn)) {
                double similarity = calculateCosineSimilarity(matrix, targetIsbn, isbn);
                similarities.put(isbn, similarity);
            }
        }
        return similarities;
    }


    private double calculateCosineSimilarity(Map<Long, Map<String, Double>> matrix, String isbn1, String isbn2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (Map<String, Double> ratings : matrix.values()) {
            Double rating1 = ratings.get(isbn1);
            Double rating2 = ratings.get(isbn2);
            if (rating1 != null && rating2 != null) {
                dotProduct += rating1 * rating2;
                normA += Math.pow(rating1, 2);
                normB += Math.pow(rating2, 2);
            }
        }
        return (normA != 0 && normB != 0) ? dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)) : 0.0;
    }
}
