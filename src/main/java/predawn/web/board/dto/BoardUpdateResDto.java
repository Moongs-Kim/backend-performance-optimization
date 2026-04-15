package predawn.web.board.dto;

import lombok.Getter;
import predawn.application.file.dto.UploadFileResult;
import predawn.domain.board.entity.Board;
import predawn.domain.board.enums.BoardOpen;
import predawn.domain.board.enums.CategoryName;

import java.util.List;

@Getter
public class BoardUpdateResDto {
    private Long boardId;
    private CategoryName categoryName;
    private BoardOpen boardOpen;
    private String title;
    private String content;
    private List<UploadFileResult> fileResponses;

    public BoardUpdateResDto(Board board) {
        this.boardId = board.getId();
        this.categoryName = board.getCategory().getCategoryName();
        this.boardOpen = board.getBoardOpen();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.fileResponses = board.getAttachFiles().stream()
                .map(attachFile -> new UploadFileResult(attachFile.getUploadFile()))
                .toList();
    }
}
