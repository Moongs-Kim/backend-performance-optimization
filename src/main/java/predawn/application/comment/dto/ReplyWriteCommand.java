package predawn.application.comment.dto;

import lombok.Getter;

@Getter
public class ReplyWriteCommand {
    private Long memberId;
    private Long commentId;
    private String replyContent;

    public ReplyWriteCommand(Long memberId, Long commentId, String replyContent) {
        this.memberId = memberId;
        this.commentId = commentId;
        this.replyContent = replyContent;
    }
}
