package capstone.bookitty.domain.star.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "star",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_member_isbn", columnNames = {"member_id", "isbn"})
        }
)
public class Star {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String isbn;
    private double score;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @Builder
    private Star(Long memberId, String isbn, double score) {
        validateStarInfo(memberId, isbn, score);

        this.memberId = memberId;
        this.isbn = isbn;
        this.score = score;
    }

    public void updateStar(double score) {
        validateScore(score);
        this.score = score;
    }

    //== private methods ==//

    private void validateStarInfo(Long memberId, String isbn, double score) {
        validateMemberId(memberId);
        validateIsbn(isbn);
        validateScore(score);
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null)
            throw new IllegalArgumentException("Member ID cannot be null");
    }

    private void validateIsbn(String isbn) {
        if (!StringUtils.hasText(isbn)) // ISBN이 비어있거나 null일 수 없음
            throw new IllegalArgumentException("ISBN cannot be null or empty");

        if (isbn.length() != 13)  // ISBN은 정확히 13자리여야 함
            throw new IllegalArgumentException("ISBN must be exactly 13 characters");

        if (!isbn.matches("\\d{13}")) // 숫자로만 구성되어야 함
            throw new IllegalArgumentException("ISBN must contain only digits");

        if (!isbn.startsWith("978") && !isbn.startsWith("979")) // 978 또는 979로 시작해야 함
            throw new IllegalArgumentException("ISBN must start with 978 or 979");

    }

    private void validateScore(double score) {
        if (score < 0.5 || score > 5.0)
            throw new IllegalArgumentException("Score must be between 0.5 and 5.0");

        // 0.5 단위로만 허용 (0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0)
        double multipliedScore = score * 2;
        if (multipliedScore != Math.floor(multipliedScore)) {
            throw new IllegalArgumentException("Score must be in 0.5 increments (0.5, 1.0, 1.5, ..., 5.0)");
        }
    }
}
