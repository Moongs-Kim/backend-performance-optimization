package predawn.web.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentWriteReqDto {
    @NotBlank(message = "댓글을 입력해 주세요")
    private String content;

    public CommentWriteReqDto(String content) {
        this.content = content;
    }
}
