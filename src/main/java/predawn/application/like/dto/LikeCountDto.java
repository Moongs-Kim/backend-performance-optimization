package predawn.application.like.dto;

import lombok.Getter;

@Getter
public class LikeCountDto {
    private Long boardId;
    private Long likeCount;

    public LikeCountDto(Long boardId, Long likeCount) {
        this.boardId = boardId;
        this.likeCount = likeCount;
    }
}
