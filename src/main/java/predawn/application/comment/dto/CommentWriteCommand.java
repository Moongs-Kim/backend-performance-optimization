package predawn.application.comment.dto;

import lombok.Getter;

@Getter
public class CommentWriteCommand {
    private Long memberId;
    private Long boardId;
    private String commentContent;

    public CommentWriteCommand(Long memberId, Long boardId, String commentContent) {
        this.memberId = memberId;
        this.boardId = boardId;
        this.commentContent = commentContent;
    }
}
