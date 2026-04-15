package predawn.web.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import predawn.application.comment.dto.CommentQueryDto;
import predawn.application.comment.dto.CommentResult;
import predawn.application.comment.dto.CommentWriteCommand;
import predawn.application.comment.service.CommentService;
import predawn.global.error.exception.ValidationException;
import predawn.web.comment.dto.CommentWriteReqDto;
import predawn.web.member.session.LoginMember;
import predawn.web.member.session.SessionConst;

import java.net.URI;

import static predawn.web.comment.mapper.CommentCommandMapper.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentRestController {

    private final CommentService commentService;

    @GetMapping("/api/board/{boardId}/comment")
    public ResponseEntity<CommentResult> getMoreComment(@PathVariable Long boardId, @RequestParam Long lastCommentId) {
        int pageSize = 3;
        CommentResult commentResult = commentService.moreComment(boardId, lastCommentId, pageSize);

        return ResponseEntity.ok(commentResult);
    }

    @PostMapping("/api/board/{boardId}/comment")
    public ResponseEntity<CommentQueryDto> commentWrite(
            @PathVariable Long boardId,
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER) LoginMember loginMember,
            @Valid @RequestBody CommentWriteReqDto commentWriteDto,
            BindingResult bindingResult)
    {
        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult, "Comment Write Validation Failed");

        CommentWriteCommand commentWriteCommand = toCommentWriteCommand(loginMember, boardId, commentWriteDto);

        CommentQueryDto commentQueryDto = commentService.postComment(commentWriteCommand);

        return ResponseEntity
                .created(URI.create("/board/" + boardId + "/comments/" + commentQueryDto.getCommentId()))
                .body(commentQueryDto);
    }
}
