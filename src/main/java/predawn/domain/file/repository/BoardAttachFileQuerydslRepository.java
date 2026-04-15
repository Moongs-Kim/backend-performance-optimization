package predawn.domain.file.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import predawn.domain.file.entity.BoardAttachFile;

import java.util.List;

import static predawn.domain.file.entity.QBoardAttachFile.boardAttachFile;

@Repository
public class BoardAttachFileQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public BoardAttachFileQuerydslRepository(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    public List<BoardAttachFile> findAllByBoardId(Long boardId) {
        return queryFactory
                .selectFrom(boardAttachFile)
                .where(boardAttachFile.board.id.eq(boardId))
                .fetch();
    }
}
