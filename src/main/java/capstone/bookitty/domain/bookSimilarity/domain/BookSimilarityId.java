package capstone.bookitty.domain.bookSimilarity.domain;


import lombok.*;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class BookSimilarityId implements Serializable {
    private String isbn1;
    private String isbn2;
}