package predawn.application.comment.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import predawn.application.comment.dto.CommentQueryDto;
import predawn.application.comment.dto.CommentResult;
import predawn.application.comment.dto.CommentWriteCommand;
import predawn.application.comment.dto.ReplyCountDto;
import predawn.domain.board.entity.Board;
import predawn.domain.comment.entity.Comment;
import predawn.domain.comment.repository.CommentRepository;
import predawn.domain.comment.repository.ReplyRepository;
import predawn.domain.member.entity.Member;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final EntityManager entityManager;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    @Transactional
    public CommentQueryDto postComment(CommentWriteCommand commentWriteCommand) {

        Member member = entityManager.getReference(Member.class, commentWriteCommand.getMemberId());
        Board board = entityManager.getReference(Board.class, commentWriteCommand.getBoardId());

        Comment savedComment = commentRepository.save(new Comment(member, board, commentWriteCommand.getCommentContent()));

        return new CommentQueryDto(savedComment);
    }

    @Transactional(readOnly = true)
    public CommentResult moreComment(Long boardId, Long lastCommentId, int pageSize) {
        //commentValidation(boardId, lastCommentId);

        List<Comment> comments = commentRepository.findCommentsByCursor(boardId, lastCommentId, pageSize + 1);

        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .toList();

        Map<Long, Integer> replyCountMap = replyRepository.countByCommentIds(commentIds).stream()
                .collect(Collectors.toMap(
                        ReplyCountDto::getCommentId,
                        ReplyCountDto::getReplyCount
                ));

        List<CommentQueryDto> commentQueryDtos = comments.stream()
                .map(comment -> new CommentQueryDto(comment, replyCountMap))
                .toList();

        return new CommentResult(commentQueryDtos, pageSize);
    }
}
