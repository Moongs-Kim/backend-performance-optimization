package predawn.domain.like.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import predawn.application.like.dto.LikeCountDto;

import java.util.List;

import static predawn.domain.like.entity.QLike.like;

@Repository
public class LikeQuerydslRepository {
    private final JPAQueryFactory queryFactory;

    public LikeQuerydslRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<LikeCountDto> countByBoardIds(List<Long> boardIds) {
        return queryFactory
                .select(Projections.constructor(LikeCountDto.class,
                        like.board.id.as("boardId"),
                        like.count().as("likeCount")
                ))
                .from(like)
                .where(like.board.id.in(boardIds))
                .groupBy(like.board.id)
                .fetch();
    }
}
