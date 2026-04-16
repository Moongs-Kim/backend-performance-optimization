package predawn.domain.board.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import predawn.application.board.dto.BoardListQueryDto;
import predawn.application.board.dto.BoardSearchCond;
import predawn.application.board.enums.SearchType;
import predawn.application.board.enums.SortType;
import predawn.domain.board.entity.Board;
import predawn.domain.board.enums.BoardOpen;

import java.util.List;
import java.util.Optional;

import static predawn.domain.board.entity.QBoard.board;
import static predawn.domain.board.entity.QCategory.category;
import static predawn.domain.file.entity.QBoardAttachFile.boardAttachFile;
import static predawn.domain.file.entity.QUploadFile.uploadFile;
import static predawn.domain.member.entity.QMember.member;


@Repository
public class BoardQuerydslRepository {

    private static final int LATEST_TOP_COUNT = 100;
    private final JPAQueryFactory queryFactory;

    public BoardQuerydslRepository(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    public Page<BoardListQueryDto> searchBoardsByComplex(BoardSearchCond boardSearchCond, Pageable pageable) {
        List<BoardListQueryDto> content = queryFactory
                .select(Projections.constructor(BoardListQueryDto.class,
                            board.id,
                            board.title,
                            board.viewCount,
                            board.createdDate,
                            member.name
                        )
                    )
                .from(board)
                .join(board.member, member)
                .where(containsKeywordBy(boardSearchCond.getSearchType(), boardSearchCond.getKeyword()))
                .orderBy(sortBy(boardSearchCond.getSortType()), board.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> query = queryFactory
                .select(board.count())
                .from(board);

        if (boardSearchCond.getSearchType() == SearchType.WRITER) {
            query.join(board.member, member);
        }

        JPAQuery<Long> countQuery = query.where(
                containsKeywordBy(boardSearchCond.getSearchType(), boardSearchCond.getKeyword())
        );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression containsKeywordBy(SearchType searchType, String keyword) {
        return switch (searchType) {
            case TITLE -> board.title.contains(keyword);
            case WRITER -> member.name.contains(keyword);
            case null -> null;
        };
    }

    private OrderSpecifier<?> sortBy(SortType sortType) {
        return switch (sortType) {
            case SortType.LATEST, SortType.LATEST_TOP_N_LIKE_COUNT -> board.createdDate.desc();
            case SortType.VIEW_COUNT -> board.viewCount.desc();
        };
    }

    public List<Long> searchBoardIdsTopNBy(BoardSearchCond boardSearchCond) {
        return queryFactory
                .select(board.id)
                .from(board)
                .join(board.member, member)
                .where(containsKeywordBy(boardSearchCond.getSearchType(), boardSearchCond.getKeyword()))
                .orderBy(sortBy(boardSearchCond.getSortType()), board.id.asc())
                .limit(LATEST_TOP_COUNT)
                .fetch();
    }

    public List<Long> findBoardIdsTopN(int latestTopCount) {
        return queryFactory
                .select(board.id)
                .from(board)
                .join(board.member, member)
                .where(board.boardOpen.eq(BoardOpen.ALL))
                .orderBy(board.createdDate.desc(), board.id.asc())
                .limit(latestTopCount)
                .fetch();
    }

    public Optional<Board> findBoardWithAttachFile(Long boardId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(board).distinct()
                .join(board.category, category).fetchJoin()
                .leftJoin(board.attachFiles, boardAttachFile).fetchJoin()
                .leftJoin(boardAttachFile.uploadFile, uploadFile).fetchJoin()
                .where(board.id.eq(boardId))
                .fetchOne());
    }


}
