package predawn.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import predawn.domain.comment.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c" +
           " WHERE c.board.id = :boardId" +
           " ORDER BY c.id DESC " +
           " LIMIT :size")
    List<Comment> findByBoardId(@Param("boardId") Long boardId, @Param("size") int pageSize);

    @Query("SELECT c FROM Comment c" +
           " WHERE c.board.id = :boardId" +
           " AND c.id < :lastCommentId" +
           " ORDER BY c.id DESC" +
           " LIMIT :size")
    List<Comment> findCommentsByCursor(@Param("boardId") Long boardId,
                                       @Param("lastCommentId") Long lastCommentId,
                                       @Param("size") int size);
}
