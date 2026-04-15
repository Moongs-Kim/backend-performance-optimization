package predawn.web.comment.mapper;

import predawn.application.comment.dto.CommentWriteCommand;
import predawn.application.comment.dto.ReplyWriteCommand;
import predawn.web.comment.dto.CommentWriteReqDto;
import predawn.web.comment.dto.ReplyWriteReqDto;
import predawn.web.member.session.LoginMember;

public abstract class CommentCommandMapper {

    public static CommentWriteCommand toCommentWriteCommand(LoginMember loginMember, Long boardId, CommentWriteReqDto commentWriteDto) {
        return new CommentWriteCommand(
                loginMember.getId(),
                boardId,
                commentWriteDto.getContent()
        );
    }

    public static ReplyWriteCommand toReplyWriteCommand(LoginMember loginMember, Long commentId, ReplyWriteReqDto replyWriteDto) {
        return new ReplyWriteCommand(
                loginMember.getId(),
                commentId,
                replyWriteDto.getContent()
        );
    }

}
