package predawn.application.board.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class BoardListQueryDto {

    private Long boardId;
    private String title;
    private String memberName;
    private LocalDate createdBoardDate;
    private Integer viewCount;
    private Long likeCount;

    public BoardListQueryDto(Long boardId, String title, Integer viewCount, LocalDateTime createdBoardDate, String memberName) {
        this.boardId = boardId;
        this.title = title;
        this.viewCount = viewCount;
        this.createdBoardDate = createdBoardDate.toLocalDate();
        this.memberName = memberName;
    }

    public BoardListQueryDto(Long boardId, String title, Integer viewCount, LocalDateTime createdBoardDate, String memberName, Long likeCount) {
        this(boardId, title, viewCount, createdBoardDate, memberName);
        this.likeCount = likeCount;
    }

    public void applyLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
}
