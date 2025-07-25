package capstone.bookitty.domain.bookSimilarity.domain;

import capstone.bookitty.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(BookSimilarityId.class)
@Table(
        name = "book_similarity",
        indexes = {
                @Index(name = "idx_isbn1", columnList = "isbn1"),
                @Index(name = "idx_isbn2", columnList = "isbn2"),
        }
)
public class BookSimilarity extends BaseEntity {

    @Id
    @Column(length = 13, nullable = false)
    private String isbn1;

    @Id
    @Column(length = 13, nullable = false)
    private String isbn2;

    @Column(nullable = false)
    private double similarity;

    @Builder
    public BookSimilarity(String isbn1, String isbn2, double similarity) {
        validateSimilarity(similarity);
        validateIsbns(isbn1, isbn2);

        // isbn1이 isbn2보다 항상 작도록 정렬
        if (isbn1.compareTo(isbn2) <= 0) {
            this.isbn1 = isbn1;
            this.isbn2 = isbn2;
        } else {
            this.isbn1 = isbn2;
            this.isbn2 = isbn1;
        }
        this.similarity = similarity;
    }

    public void updateSimilarity(double similarity) {
        validateSimilarity(similarity);
        this.similarity = similarity;
    }

    //== private methods ==//

    private void validateSimilarity(double similarity) {
        if (similarity < -1.0 || similarity > 1.0) {
            throw new IllegalArgumentException("Similarity must be between -1.0 and 1.0");
        }
    }

    private void validateIsbns(String isbn1, String isbn2) {
        if (isbn1 == null || isbn2 == null) {
            throw new IllegalArgumentException("ISBNs cannot be null");
        }
        if (isbn1.equals(isbn2)) {
            throw new IllegalArgumentException("Cannot create similarity for the same book");
        }
        validateIsbnFormat(isbn1);
        validateIsbnFormat(isbn2);
    }

    private void validateIsbnFormat(String isbn) {
        if (isbn.length() != 13) {
            throw new IllegalArgumentException("ISBN must be exactly 13 characters");
        }
        if (!isbn.matches("\\d{13}")) {
            throw new IllegalArgumentException("ISBN must contain only digits");
        }
        if (!isbn.startsWith("978") && !isbn.startsWith("979")) {
            throw new IllegalArgumentException("ISBN must start with 978 or 979");
        }
    }
}