package predawn.application.comment.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CommentResult {
    private List<CommentQueryDto> comments;
    private boolean hasNext;
    private Long lastCommentId;

    public CommentResult(List<CommentQueryDto> comments, int pageSize) {
        this.hasNext = comments.size() > pageSize;
        this.comments = hasNext ? comments.subList(0, pageSize) : comments;
        this.lastCommentId = takeLastCommentId(this.comments);
    }

    private Long takeLastCommentId(List<CommentQueryDto> commentQueryDtos) {
        if (commentQueryDtos.isEmpty()) return null;
        return commentQueryDtos.getLast().getCommentId();
    }
}


