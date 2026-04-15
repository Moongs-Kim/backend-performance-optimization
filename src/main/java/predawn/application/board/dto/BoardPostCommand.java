package predawn.application.board.dto;

import lombok.Getter;
import predawn.domain.board.enums.BoardOpen;
import predawn.domain.board.enums.CategoryName;


@Getter
public class BoardPostCommand {

    private String title;
    private String content;
    private BoardOpen boardOpen = BoardOpen.ALL;
    private CategoryName categoryName;

    public BoardPostCommand(String title, String content, BoardOpen boardOpen, CategoryName categoryName) {
        this.title = title;
        this.content = content;
        this.boardOpen = boardOpen;
        this.categoryName = categoryName;
    }
}
