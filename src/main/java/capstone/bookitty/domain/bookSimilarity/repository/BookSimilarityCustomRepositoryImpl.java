package capstone.bookitty.domain.bookSimilarity.repository;

import capstone.bookitty.domain.bookSimilarity.domain.BookSimilarity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static capstone.bookitty.domain.bookSimilarity.domain.QBookSimilarity.*;


@Repository
@RequiredArgsConstructor
public class BookSimilarityCustomRepositoryImpl implements BookSimilarityCustomRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BookSimilarity> findTopSimilarBooks(String isbn) {
        return queryFactory
                .select(bookSimilarity)
                .from(bookSimilarity)
                .where(bookSimilarity.isbn1.eq(isbn).or(bookSimilarity.isbn2.eq(isbn)))
                .orderBy(bookSimilarity.similarity.desc())
                .fetch();
    }

}
