package capstone.bookitty.domain.star.repository;

import capstone.bookitty.domain.book.api.dto.RatingPair;
import capstone.bookitty.domain.star.domain.QStar;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StarCustomRepositoryImpl implements StarCustomRepository {

    private final JPAQueryFactory queryFactory;
    private final QStar s1 = new QStar("s1");
    private final QStar s2 = new QStar("s2");

    @Override
    public List<RatingPair> findCommonRatings(String isbn1, String isbn2) {
        return queryFactory
                .select(Projections.constructor(RatingPair.class, s1.score, s2.score))
                .from(s1)
                .join(s2)
                //.on(s1.member.id.eq(s2.member.id))
                .where(s1.isbn.eq(isbn1), s2.isbn.eq(isbn2))
                .fetch();
    }

    @Override
    public List<String> findIsbnsRatedWith(String isbn) {
        return queryFactory
                .select(s2.isbn)
                .distinct()
                .from(s1)
                .join(s2)
                // .on(s1.member.id.eq(s2.member.id))
                .where(s1.isbn.eq(isbn).and(s2.isbn.ne(isbn)))
                .fetch();
    }

}
