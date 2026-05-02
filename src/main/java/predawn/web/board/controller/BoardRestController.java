package predawn.web.board.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import predawn.application.board.dto.BoardPostCommand;
import predawn.application.board.dto.BoardUpdateCommand;
import predawn.application.board.service.BoardService;
import predawn.application.file.dto.UploadFileResult;
import predawn.application.like.dto.LikeToggleResult;
import predawn.domain.file.enums.DownloadType;
import predawn.domain.file.exception.FileNotSelectedException;
import predawn.domain.file.vo.DownloadFile;
import predawn.global.error.exception.ValidationException;
import predawn.infrastructure.redis.BoardRedisRepository;
import predawn.web.board.dto.BoardPostForm;
import predawn.web.board.dto.BoardUpdateReqDto;
import predawn.web.board.mapper.BoardCommandMapper;
import predawn.web.member.session.LoginMember;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static predawn.web.member.session.SessionConst.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BoardRestController {

    private final BoardService boardService;
    private final BoardRedisRepository boardRedisRepository;

    @PostMapping("/api/board/write")
    public ResponseEntity<String> boardWrite(
            @Valid @ModelAttribute("postForm") BoardPostForm postForm,
            BindingResult bindingResult,
            @SessionAttribute(name = LOGIN_MEMBER) LoginMember loginMember) {

        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult, "Board Write Validation Failed");

        BoardPostCommand postCommand = BoardCommandMapper.toPostCommand(postForm);

        Long boardId = boardService.postBoard(loginMember.getId(), postCommand, postForm.getAttachFiles());

        return ResponseEntity.created(URI.create("/boards/" + boardId)).build();
    }

    @PutMapping("/api/board/{boardId}/like")
    public ResponseEntity<LikeToggleResult> addLike(
            @PathVariable Long boardId,
            HttpSession session)
    {
        LoginMember loginMember = (LoginMember) session.getAttribute(LOGIN_MEMBER);

        LikeToggleResult likeToggleResult = boardService.incrementLikeCount(boardId, loginMember.getId());

        return responseOf(likeToggleResult);
    }

    @DeleteMapping("/api/board/{boardId}/like")
    public ResponseEntity<LikeToggleResult> removeLike(
            @PathVariable Long boardId,
            HttpSession session)
    {
        LoginMember loginMember = (LoginMember) session.getAttribute(LOGIN_MEMBER);

        LikeToggleResult likeToggleResult = boardService.decrementLikeCount(boardId, loginMember.getId());

        return responseOf(likeToggleResult);
    }

    @PatchMapping("/api/board/{boardId}")
    public ResponseEntity<Void> boardUpdate(
            @PathVariable Long boardId,
            @Validated @ModelAttribute BoardUpdateReqDto boardUpdateDto, BindingResult bindingResult,
            @SessionAttribute(name = LOGIN_MEMBER) LoginMember loginMember)
    {
        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult, "Board Update Validation Failed");

        BoardUpdateCommand boardUpdateCommand = BoardCommandMapper.toUpdateCommand(boardId, boardUpdateDto);

        boardService.updateBoard(boardUpdateCommand, loginMember.getId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/board/{boardId}/file")
    public ResponseEntity<UploadFileResponse> boardFileUpdate(
            @PathVariable Long boardId,
            @RequestParam(required = false) List<MultipartFile> multipartFiles,
            @SessionAttribute(name = LOGIN_MEMBER) LoginMember loginMember)
    {
        if (multipartFiles == null) throw new FileNotSelectedException();

        List<UploadFileResult> uploadFileResponse = boardService.updateBoardFile(boardId, loginMember.getId(), multipartFiles);

        return ResponseEntity.ok().body(new UploadFileResponse(uploadFileResponse));
    }

    @DeleteMapping("/api/board/{boardId}/file/{fileId}")
    public ResponseEntity<Void> boardFileDelete(
            @PathVariable Long boardId, @PathVariable Long fileId,
            @SessionAttribute(name = LOGIN_MEMBER) LoginMember loginMember)
    {
        boardService.deleteBoardFile(boardId, fileId, loginMember.getId());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/board/{boardId}")
    public ResponseEntity<Void> boardDelete(
            @PathVariable Long boardId,
            @SessionAttribute(name = LOGIN_MEMBER) LoginMember loginMember)
    {
        boardService.deleteBoard(boardId, loginMember.getId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/board/{boardId}/file/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long boardId, @PathVariable Long fileId) throws MalformedURLException {
        DownloadFile downloadFile = boardService.downloadFile(boardId, fileId);

        if (downloadFile.getType() == DownloadType.EXTERNAL) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(downloadFile.getPath()))
                    .build();
        }

        String filename = downloadFile.getFilename();
        String encodedUploadFileName = UriUtils.encode(filename, StandardCharsets.UTF_8);

        Resource resource = new UrlResource("file:" + downloadFile.getPath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedUploadFileName + "\"")
                .contentType(MediaType.parseMediaType(downloadFile.getContentType()))
                .body(resource);
    }

    @PostMapping("/api/board/{boardId}/view")
    public void increaseViewCount(
            @PathVariable Long boardId,
            @SessionAttribute(name = LOGIN_MEMBER) LoginMember loginMember
    ) {
        Boolean isSaved = boardRedisRepository.saveViewIfNotExists(boardId, loginMember.getId());

        if (isSaved) {
            boardService.incrementViewCount(boardId, loginMember.getId());
        }
    }

    private ResponseEntity<LikeToggleResult> responseOf(LikeToggleResult likeToggleResult) {
        return (likeToggleResult == null)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(likeToggleResult);
    }

    @Getter
    static class UploadFileResponse {
        private final List<UploadFileResult> uploadFiles;

        public UploadFileResponse(List<UploadFileResult> uploadFiles) {
            this.uploadFiles = uploadFiles;
        }
    }
}
