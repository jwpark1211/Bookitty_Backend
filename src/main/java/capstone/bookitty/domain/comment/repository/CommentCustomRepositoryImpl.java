package capstone.bookitty.domain.comment.repository;

import capstone.bookitty.domain.comment.domain.Comment;
import capstone.bookitty.domain.comment.domain.QComment;
import capstone.bookitty.domain.member.domain.QMember;
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
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final JPAQueryFactory queryFactory;
    private final QComment comment = QComment.comment;
    private final QMember member = QMember.member;

    @Override
    public Page<Comment> findByFilters(String isbn, Long memberId, Pageable pageable) {
        BooleanBuilder whereClause = new BooleanBuilder();

        if (isbn != null && !isbn.trim().isEmpty())  whereClause.and(comment.isbn.eq(isbn));
        if (memberId != null) whereClause.and(comment.member.id.eq(memberId));

        List<Comment> comments = queryFactory
                .selectFrom(comment)
                .leftJoin(comment.member, member).fetchJoin()
                .where(whereClause)
                .orderBy(comment.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(comment.count())
                .from(comment)
                .where(whereClause)
                .fetchOne();

        return new PageImpl<>(comments, pageable, total != null ? total : 0);
    }
}
