package predawn.domain.comment.repository;

import predawn.domain.comment.entity.Reply;

import java.util.List;

public interface ReplyRepositoryQuerydsl {

    List<Reply> findRepliesByCursor(Long commentId, Long lastReplyId, Long maxReplyId, int size);
}
