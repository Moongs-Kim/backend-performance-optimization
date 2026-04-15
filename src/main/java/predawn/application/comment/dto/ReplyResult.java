package predawn.application.comment.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ReplyResult {

    private List<ReplyQueryDto> replies;
    private boolean hasNext;
    private Long lastReplyId;
    private Long maxReplyId;

    public ReplyResult(List<ReplyQueryDto> replies, Long maxReplyId, int pageSize) {
        this.hasNext = replies.size() > pageSize;
        this.replies = hasNext ? replies.subList(0, pageSize) : replies;
        this.lastReplyId = takeLastReplyId(this.replies);
        this.maxReplyId = maxReplyId;
    }

    private Long takeLastReplyId(List<ReplyQueryDto> replies) {
        if (replies.isEmpty()) return null;
        return replies.getLast().getReplyId();
    }
}
