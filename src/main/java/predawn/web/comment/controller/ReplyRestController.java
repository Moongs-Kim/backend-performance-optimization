package predawn.web.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import predawn.application.comment.dto.ReplyQueryDto;
import predawn.application.comment.dto.ReplyResult;
import predawn.application.comment.dto.ReplyWriteCommand;
import predawn.application.comment.service.ReplyService;
import predawn.global.error.exception.ValidationException;
import predawn.web.comment.dto.ReplyWriteReqDto;
import predawn.web.member.session.LoginMember;
import predawn.web.member.session.SessionConst;

import java.net.URI;

import static predawn.web.comment.mapper.CommentCommandMapper.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReplyRestController {

    private final ReplyService replyService;

    @GetMapping("/api/comment/{commentId}/reply")
    public ResponseEntity<ReplyResult> moreReplyAPI(
            @PathVariable Long commentId,
            @RequestParam(required = false) Long lastReplyId,
            @RequestParam(required = false) Long maxReplyId)
    {
        int pageSize = 3;
        ReplyResult replyResult = replyService.moreReply(commentId, lastReplyId, maxReplyId, pageSize);

        return ResponseEntity.ok(replyResult);
    }

    @PostMapping("/api/comment/{commentId}/reply")
    public ResponseEntity<ReplyQueryDto> replyWrite(
            @PathVariable Long commentId,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER) LoginMember loginMember,
            @Valid @RequestBody ReplyWriteReqDto replyWriteDto,
            BindingResult bindingResult)
    {
        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult, "Reply Write Validation Failed");

        ReplyWriteCommand replyWriteCommand = toReplyWriteCommand(loginMember, commentId, replyWriteDto);

        ReplyQueryDto replyQueryDto = replyService.postReply(replyWriteCommand);

        return ResponseEntity
                .created(URI.create("/comments/" + commentId + "/reply/" + replyQueryDto.getReplyId()))
                .body(replyQueryDto);
    }
}
