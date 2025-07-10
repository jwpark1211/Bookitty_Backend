package capstone.bookitty.domain.star.repository;

import capstone.bookitty.domain.book.dto.BookPair;
import capstone.bookitty.domain.book.dto.RatingPair;
import capstone.bookitty.domain.star.domain.QStar;
import capstone.bookitty.domain.star.domain.Star;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StarCustomRepositoryImpl implements StarCustomRepository {

    private final JPAQueryFactory queryFactory;
    private final QStar s1 = new QStar("s1");
    private final QStar s2 = new QStar("s2");

    @Override
    public Page<BookPair> findDistinctBookPairs(Pageable pageable) {
        // 페이징 처리된 BookPair 리스트 조회
        List<BookPair> bookPairList = queryFactory
                .select(Projections.constructor(BookPair.class, s1.isbn, s2.isbn))
                .from(s1)
                .join(s2)
                .on(s1.member.id.eq(s2.member.id))
                .where(s1.isbn.lt(s2.isbn))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 건수를 조회하는 count 쿼리
        Long total = queryFactory
                .select(s1.isbn.count())
                .from(s1)
                .join(s2)
                .on(s1.member.id.eq(s2.member.id))
                .where(s1.isbn.lt(s2.isbn))
                .fetchOne();

        return new PageImpl<>(bookPairList, pageable, total != null ? total : 0);
    }

    @Override
    public Page<Star> findByFilters(String isbn, Long memberId, Pageable pageable) {
        BooleanBuilder whereClause = new BooleanBuilder();

        if (isbn != null) {
            whereClause.and(s1.isbn.eq(isbn));
        }
        if (memberId != null) {
            whereClause.and(s1.member.id.eq(memberId));
        }

        List<Star> starList = queryFactory
                .selectFrom(s1)
                .where(whereClause)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(s1.count())
                .from(s1)
                .where(whereClause)
                .fetchOne();

        return new PageImpl<>(starList, pageable, total != null ? total : 0);
    }

    @Override
    public List<RatingPair> findCommonRatings(String isbn1, String isbn2) {
        return queryFactory
                .select(Projections.constructor(RatingPair.class, s1.score, s2.score))
                .from(s1)
                .join(s2)
                .on(s1.member.id.eq(s2.member.id))
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
                .on(s1.member.id.eq(s2.member.id))
                .where(s1.isbn.eq(isbn).and(s2.isbn.ne(isbn)))
                .fetch();
    }

}
