package predawn.application.board.dto;

import lombok.Getter;
import predawn.application.comment.dto.CommentResult;
import predawn.application.file.dto.UploadFileResult;
import predawn.domain.board.entity.Board;
import predawn.domain.file.entity.BoardAttachFile;
import predawn.global.util.TimeFormatter;

import java.util.List;

@Getter
public class BoardDetailQueryDto {

    private Long boardId;
    private Long writerId;
    private String title;
    private String boardWriter;
    private String boardCreatedTime;
    private int viewCount;
    private String boardContent;
    private List<UploadFileResult> uploadFiles;
    private boolean isLiked;
    private long likeCount;
    private CommentResult commentResult;

    public BoardDetailQueryDto(Board board, Long likeCount, boolean isLiked, CommentResult commentResult) {
        this.boardId = board.getId();
        this.writerId = board.getMember().getId();
        this.title = board.getTitle();
        this.boardWriter = board.getMember().getName();
        this.boardCreatedTime = TimeFormatter.format(board.getCreatedDate());
        this.viewCount = board.getViewCount();
        this.boardContent = board.getContent();
        this.uploadFiles = mapToUploadFile(board.getAttachFiles());
        this.isLiked = isLiked;
        this.likeCount = likeCount;
        this.commentResult = commentResult;
    }

    private List<UploadFileResult> mapToUploadFile(List<BoardAttachFile> boardAttachFileList) {
        return boardAttachFileList.stream()
                .map(attachFile -> new UploadFileResult(attachFile.getUploadFile()))
                .toList();
    }
}
