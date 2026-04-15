package predawn.application.comment.dto;

import lombok.Getter;
import predawn.domain.comment.entity.Comment;
import predawn.global.util.TimeFormatter;

import java.util.Map;

@Getter
public class CommentQueryDto {

    private Long commentId;
    private String commentWriter;
    private String commentCreatedTime;
    private String commentContent;
    private long replyCount;

    public CommentQueryDto(Comment comment) {
        this.commentId = comment.getId();
        this.commentWriter = comment.getMember().getName();
        this.commentCreatedTime = TimeFormatter.format(comment.getCreatedDate());
        this.commentContent = comment.getContent();
    }

    public CommentQueryDto(Comment comment, Map<Long, Integer> replyCountMap) {
        this.commentId = comment.getId();
        this.commentWriter = comment.getMember().getName();
        this.commentCreatedTime = TimeFormatter.format(comment.getCreatedDate());
        this.commentContent = comment.getContent();
        this.replyCount = replyCountMap.getOrDefault(commentId, 0);
    }
}
