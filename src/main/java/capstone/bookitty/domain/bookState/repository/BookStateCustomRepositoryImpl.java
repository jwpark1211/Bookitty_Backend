package capstone.bookitty.domain.bookState.repository;

import capstone.bookitty.domain.bookState.domain.BookState;
import capstone.bookitty.domain.bookState.domain.QBookState;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookStateCustomRepositoryImpl implements BookStateCustomRepository{

    private final JPAQueryFactory queryFactory;
    private final QBookState bs = QBookState.bookState;

    @Override
    public Page<BookState> findByFilters(String isbn, Long memberId, Pageable pageable) {

      BooleanBuilder whereClause = new BooleanBuilder();

        if (isbn != null) {
            whereClause.and(bs.isbn.eq(isbn));
        }
        if (memberId != null) {
            whereClause.and(bs.member.id.eq(memberId));
        }

        List<BookState> bookStateList = queryFactory
                .selectFrom(bs)
                .where(whereClause)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(bs.count())
                .from(bs)
                .where(whereClause)
                .fetchOne();

        return new PageImpl<>(bookStateList, pageable, total != null ? total : 0);
    }
}
