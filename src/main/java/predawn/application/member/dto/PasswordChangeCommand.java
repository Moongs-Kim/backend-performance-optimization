package predawn.application.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PasswordChangeCommand {
    private String token;
    private String password;

    @Builder
    private PasswordChangeCommand(String token, String password) {
        this.token = token;
        this.password = password;
    }

    public static PasswordChangeCommand of(String token, String password) {
        return PasswordChangeCommand.builder()
                .token(token)
                .password(password)
                .build();
    }
}
