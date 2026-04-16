package predawn.domain.board.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import predawn.application.board.dto.BoardListQueryDto;
import predawn.domain.board.entity.Board;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT DISTINCT b FROM Board b" +
           " JOIN FETCH b.member m" +
           " LEFT JOIN FETCH b.attachFiles af" +
           " LEFT JOIN FETCH af.uploadFile uf" +
           " WHERE b.id = :boardId")
    Optional<Board> findBoardWithMemberAndAttachFile(@Param("boardId") Long boardId);

    @Query("SELECT new predawn.application.board.dto.BoardListQueryDto" +
           "       (b.id, b.title, b.viewCount, b.createdDate, m.name, lc.likeCount)" +
           " FROM Board b" +
           " JOIN b.member m" +
           " LEFT join (" +
           "      SELECT l.board.id AS boardId, count(l) AS likeCount" +
           "      FROM Like l" +
           "      GROUP BY l.board.id" +
           "  ) AS lc ON lc.boardId = b.id" +
           " WHERE b.id IN :boardIds" +
           " ORDER BY lc.likeCount DESC, b.createdDate DESC, b.id")
    List<BoardListQueryDto> findTopNOrderByLikeCountDesc(
            @Param("boardIds") List<Long> boardIds, Pageable pageable
    );

    @Modifying
    @Query("UPDATE Board b SET b.viewCount = b.viewCount + 1 WHERE b.id = :boardId")
    void increaseViewCount(@Param("boardId") Long boardId);
}
