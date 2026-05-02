package predawn.web.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PasswordResetRequest {

    @NotBlank(message = "비밀번호를 입력해 주세요")
    private String newPassword;

    @NotBlank(message = "비밀번호 확인을 입력해 주세요")
    private String passwordCheck;

    @Builder
    private PasswordResetRequest(String newPassword, String passwordCheck) {
        this.newPassword = newPassword;
        this.passwordCheck = passwordCheck;
    }
}
