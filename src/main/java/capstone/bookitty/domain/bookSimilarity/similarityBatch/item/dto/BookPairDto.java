package capstone.bookitty.domain.bookSimilarity.similarityBatch.item.dto;

public record BookPairDto(String isbn1, String isbn2) {

    public static BookPairDto of(String isbn1, String isbn2) {
        // isbn1이 isbn2보다 작도록 정렬하여 생성
        if (isbn1.compareTo(isbn2) <= 0) {
            return new BookPairDto(isbn1, isbn2);
        } else {
            return new BookPairDto(isbn2, isbn1);
        }
    }
}
