package predawn.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import predawn.application.comment.dto.ReplyCountDto;
import predawn.domain.comment.entity.Reply;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long>, ReplyRepositoryQuerydsl{

    @Query("SELECT r.comment.id AS commentId, COUNT(r) AS replyCount" +
           " FROM Reply r" +
           " WHERE r.comment.id in :commentIds" +
           " GROUP BY r.comment.id")
    List<ReplyCountDto> countByCommentIds(@Param("commentIds") List<Long> commentIds);

    @Query("SELECT r.id" +
           " FROM Reply r" +
           " WHERE r.comment.id = :commentId" +
           " ORDER BY r.id DESC" +
           " LIMIT 1")
    Long findMaxReplyId(@Param("commentId") Long commentId);

}
