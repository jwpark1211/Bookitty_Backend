package capstone.bookitty.domain.bookSimilarity.domain;

import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarityId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(BookSimilarityId.class)
public class BookSimilarity {
    @Id
    private String isbn1;
    @Id
    private String isbn2;
    private double similarity;

    @Builder
    public BookSimilarity(String isbn1, String isbn2, double similarity) {
        this.isbn1 = isbn1;
        this.isbn2 = isbn2;
        this.similarity = similarity;
    }
}
