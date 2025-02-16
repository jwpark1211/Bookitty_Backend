package capstone.bookitty.domain.bookSimilarity.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class BookSimilarityId implements Serializable {
    private String isbn1;
    private String isbn2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookSimilarityId that = (BookSimilarityId) o;
        return Objects.equals(isbn1, that.isbn1) && Objects.equals(isbn2, that.isbn2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn1, isbn2);
    }
}