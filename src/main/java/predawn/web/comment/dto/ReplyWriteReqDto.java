package predawn.web.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReplyWriteReqDto {
    @NotBlank(message = "답글을 입력해 주세요")
    private String content;

    public ReplyWriteReqDto(String content) {
        this.content = content;
    }
}
