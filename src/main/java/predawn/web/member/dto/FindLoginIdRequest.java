package predawn.web.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FindLoginIdRequest {

    @NotBlank(message = "이메일을 입력해 주세요")
    @Email(message = "유효하지 않은 이메일 형식 입니다")
    private String email;

    @Builder
    private FindLoginIdRequest(String email) {
        this.email = email;
    }
}
