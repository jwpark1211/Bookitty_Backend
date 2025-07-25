package capstone.bookitty.domain.bookSimilarity.application;

import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarityId;
import capstone.bookitty.domain.bookSimilarity.repository.BookSimilarityRepository;
import capstone.bookitty.domain.bookSimilarity.similarityBatch.item.calculator.CosineSimilarityCalculator;
import capstone.bookitty.domain.star.domain.Star;
import capstone.bookitty.domain.star.repository.StarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookSimilarityService {
    
    private final BookSimilarityRepository bookSimilarityRepository;
    private final StarRepository starRepository;
    private final CosineSimilarityCalculator cosineSimilarityCalculator;
    
    private static final int MIN_COMMON_USERS = 3;
    private static final double MIN_SIMILARITY_THRESHOLD = 0.1;
    
    public BookSimilarity calculateAndSaveSimilarity(String isbn1, String isbn2) {
        return calculateAndSaveSimilarity(isbn1, isbn2, null);
    }
    
    public BookSimilarity calculateAndSaveSimilarity(String isbn1, String isbn2, Map<String, Map<Long, Double>> ratingsCache) {
        Map<Long, Double> isbn1Ratings = ratingsCache != null ? 
            ratingsCache.computeIfAbsent(isbn1, this::getRatingsMap) : getRatingsMap(isbn1);
        Map<Long, Double> isbn2Ratings = ratingsCache != null ? 
            ratingsCache.computeIfAbsent(isbn2, this::getRatingsMap) : getRatingsMap(isbn2);
        
        Set<Long> commonUsers = isbn1Ratings.keySet().stream()
                .filter(isbn2Ratings::containsKey)
                .collect(Collectors.toSet());
        
        if (commonUsers.size() < MIN_COMMON_USERS) {
            deleteSimilarityIfExists(isbn1, isbn2);
            return null;
        }
        
        double similarity = cosineSimilarityCalculator.calculate(isbn1Ratings, isbn2Ratings, commonUsers);
        
        if (Math.abs(similarity) < MIN_SIMILARITY_THRESHOLD) {
            deleteSimilarityIfExists(isbn1, isbn2);
            return null;
        }
        
        return saveOrUpdateSimilarity(isbn1, isbn2, similarity);
    }
    
    public void recalculateSimilarityForBook(String targetIsbn) {
        Map<Long, Double> targetRatings = getRatingsMap(targetIsbn);
        
        if (targetRatings.isEmpty()) {
            deleteAllSimilaritiesForBook(targetIsbn);
            return;
        }
        
        List<String> allOtherIsbns = starRepository.findDistinctIsbn()
                .stream()
                .filter(isbn -> !isbn.equals(targetIsbn))
                .collect(Collectors.toList());
        
        for (String otherIsbn : allOtherIsbns) {
            calculateAndSaveSimilarity(targetIsbn, otherIsbn);
        }
    }
    
    public Map<Long, Double> getRatingsMap(String isbn) {
        List<Star> stars = starRepository.findByIsbn(isbn);
        return stars.stream()
                .collect(Collectors.toMap(
                        Star::getMemberId,
                        Star::getScore
                ));
    }
    
    private BookSimilarity saveOrUpdateSimilarity(String isbn1, String isbn2, double similarity) {
        String firstIsbn = isbn1.compareTo(isbn2) < 0 ? isbn1 : isbn2;
        String secondIsbn = isbn1.compareTo(isbn2) < 0 ? isbn2 : isbn1;
        
        BookSimilarityId id = new BookSimilarityId(firstIsbn, secondIsbn);
        
        BookSimilarity bookSimilarity = bookSimilarityRepository.findById(id)
                .map(existing -> {
                    existing.updateSimilarity(similarity);
                    return existing;
                })
                .orElse(BookSimilarity.builder()
                        .isbn1(firstIsbn)
                        .isbn2(secondIsbn)
                        .similarity(similarity)
                        .build());
        
        return bookSimilarityRepository.save(bookSimilarity);
    }
    
    private void deleteSimilarityIfExists(String isbn1, String isbn2) {
        String firstIsbn = isbn1.compareTo(isbn2) < 0 ? isbn1 : isbn2;
        String secondIsbn = isbn1.compareTo(isbn2) < 0 ? isbn2 : isbn1;
        
        BookSimilarityId id = new BookSimilarityId(firstIsbn, secondIsbn);
        if (bookSimilarityRepository.existsById(id)) {
            bookSimilarityRepository.deleteById(id);
        }
    }
    
    private void deleteAllSimilaritiesForBook(String isbn) {
        bookSimilarityRepository.deleteByIsbn1OrIsbn2(isbn, isbn);
    }
}