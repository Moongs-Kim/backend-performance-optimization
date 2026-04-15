package predawn.web.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
public class BoardUpdateReqDto {

    @NotBlank(message = "카테고리를 선택해 주세요")
    private String categoryName;

    @NotBlank(message = "공개 여부를 선택해 주세요")
    private String boardOpen;

    @NotBlank(message = "제목을 입력해 주세요")
    @Length(max = 20, message = "제목은 20자 이하여야 합니다")
    private String title;

    @NotBlank(message = "내용을 입력해 주세요")
    @Length(max = 5000, message = "내용은 5000자 이하여야 합니다")
    private String content;

    public BoardUpdateReqDto(String categoryName, String boardOpen, String title, String content) {
        this.categoryName = categoryName;
        this.boardOpen = boardOpen;
        this.title = title;
        this.content = content;
    }
}
