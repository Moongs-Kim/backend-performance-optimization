package predawn.domain.comment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import predawn.domain.comment.entity.Reply;

import java.util.List;

import static predawn.domain.comment.entity.QReply.reply;

public class ReplyRepositoryQuerydslImpl implements ReplyRepositoryQuerydsl{

    private final JPAQueryFactory queryFactory;

    public ReplyRepositoryQuerydslImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Reply> findRepliesByCursor(Long commentId, Long lastReplyId, Long maxReplyId, int size) {
        return queryFactory
                .selectFrom(reply)
                .where(
                        reply.comment.id.eq(commentId),
                        lastReplyId != null ? reply.id.gt(lastReplyId) : null,
                        maxReplyId != null ? reply.id.loe(maxReplyId) : null
                )
                .orderBy(reply.id.asc())
                .limit(size)
                .fetch();
    }
}
