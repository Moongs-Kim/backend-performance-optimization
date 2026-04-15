package predawn.application.comment.dto;

import lombok.Getter;
import predawn.domain.comment.entity.Reply;
import predawn.global.util.TimeFormatter;

@Getter
public class ReplyQueryDto {
    private Long replyId;
    private String replyWriter;
    private String replyCreatedTime;
    private String replyContent;

    public ReplyQueryDto(Reply reply) {
        this.replyId = reply.getId();
        this.replyWriter = reply.getMember().getName();
        this.replyCreatedTime = TimeFormatter.format(reply.getCreatedDate());
        this.replyContent = reply.getContent();
    }
}
