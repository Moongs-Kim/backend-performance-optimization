package predawn.application.board.dto;

import lombok.Getter;
import predawn.domain.board.enums.BoardOpen;
import predawn.domain.board.enums.CategoryName;

@Getter
public class BoardUpdateCommand {
    private Long boardId;
    private CategoryName categoryName;
    private BoardOpen boardOpen;
    private String title;
    private String content;

    public BoardUpdateCommand(Long boardId, CategoryName categoryName, BoardOpen boardOpen, String title, String content) {
        this.boardId = boardId;
        this.categoryName = categoryName;
        this.boardOpen = boardOpen;
        this.title = title;
        this.content = content;
    }
}
