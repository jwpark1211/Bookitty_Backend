package capstone.bookitty.domain.bookSimilarity.similarityBatch.item.calculator;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class CosineSimilarityCalculator {

    /**
     * 두 평점 맵에 대한 코사인 유사도를 계산합니다.
     *
     * @param ratings1    첫 번째 아이템의 평점 맵
     * @param ratings2    두 번째 아이템의 평점 맵
     * @param commonUsers 공통 사용자 집합
     * @return 코사인 유사도 (-1.0 ~ 1.0)
     */
    public double calculate(Map<Long, Double> ratings1,
                            Map<Long, Double> ratings2,
                            Set<Long> commonUsers) {

        if (commonUsers.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        // 벡터의 내적과 크기 계산
        for (Long userId : commonUsers) {
            double rating1 = ratings1.get(userId);
            double rating2 = ratings2.get(userId);

            dotProduct += rating1 * rating2;
            magnitude1 += rating1 * rating1;
            magnitude2 += rating2 * rating2;
        }

        // 분모가 0인 경우 처리 (벡터의 크기가 0인 경우)
        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }

        // 코사인 유사도 = 내적 / (||A|| * ||B||)
        return dotProduct / (Math.sqrt(magnitude1) * Math.sqrt(magnitude2));
    }

}
