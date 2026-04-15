package predawn.web.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;
import predawn.domain.board.enums.BoardOpen;
import predawn.domain.board.enums.CategoryName;

import java.util.List;

@Getter
@Setter
public class BoardPostForm {

    @NotBlank(message = "제목을 입력해 주세요")
    @Length(max = 20, message = "제목은 20자 이하여야 합니다")
    private String title;

    @NotBlank(message = "내용을 입력해 주세요")
    @Length(max = 5000, message = "내용은 5000자 이하여야 합니다")
    private String content;

    @NotNull(message = "공개 여부를 선택해 주세요")
    private BoardOpen boardOpen;

    @NotNull(message = "카테고리를 선택해 주세요")
    private CategoryName categoryName;

    private List<MultipartFile> attachFiles;

    public BoardPostForm() {
        boardOpen = BoardOpen.ALL;
    }
}
