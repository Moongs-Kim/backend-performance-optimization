package predawn.web.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PasswordResetCheckRequest {

    @NotBlank(message = "로그인 아이디를 입력해 주세요")
    private String loginId;

    @NotBlank(message = "이메일을 입력해 주세요")
    @Email(message = "유효하지 않은 이메일 형식 입니다")
    private String email;

    @Builder
    private PasswordResetCheckRequest(String loginId, String email) {
        this.loginId = loginId;
        this.email = email;
    }
}
