package predawn.application.comment.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import predawn.application.comment.dto.ReplyQueryDto;
import predawn.application.comment.dto.ReplyResult;
import predawn.application.comment.dto.ReplyWriteCommand;
import predawn.domain.comment.entity.Comment;
import predawn.domain.comment.entity.Reply;
import predawn.domain.comment.repository.ReplyRepository;
import predawn.domain.member.entity.Member;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyService {

    private final EntityManager em;
    private final ReplyRepository replyRepository;

    @Transactional
    public ReplyQueryDto postReply(ReplyWriteCommand replyWriteCommand) {
        Member member = em.getReference(Member.class, replyWriteCommand.getMemberId());
        Comment comment = em.getReference(Comment.class, replyWriteCommand.getCommentId());

        Reply savedReply = replyRepository.save(new Reply(member, comment, replyWriteCommand.getReplyContent()));

        return new ReplyQueryDto(savedReply);
    }

    @Transactional(readOnly = true)
    public ReplyResult moreReply(Long commentId, Long lastReplyId, Long maxReplyId, int pageSize) {
        if (maxReplyId == null) maxReplyId = replyRepository.findMaxReplyId(commentId);

        List<Reply> replies = replyRepository.findRepliesByCursor(commentId, lastReplyId, maxReplyId, pageSize + 1);

        List<ReplyQueryDto> replyQueryDtos = replies.stream()
                .map(ReplyQueryDto::new)
                .toList();

        return new ReplyResult(replyQueryDtos, maxReplyId, pageSize);
    }
}
