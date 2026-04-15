package predawn.web.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import predawn.domain.member.enums.Gender;

import java.time.LocalDate;

@Getter
@Setter
public class MemberSignupForm {

    @NotBlank(message = "아이디를 입력해 주세요")
    private String loginId;

    private boolean idDuplicationCheck;

    @NotBlank(message = "비밀번호를 입력해 주세요")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해 주세요")
    private String passwordCheck;

    @NotBlank(message = "이름을 입력해 주세요")
    private String name;

    @NotNull(message = "생년월일을 입력해 주세요")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    private LocalDate birthDate;

    @NotNull(message = "성별을 선택해 주세요")
    private Gender gender;

    @NotBlank(message = "이메일을 입력해 주세요")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @NotBlank(message = "주소를 입력해 주세요")
    private String address;

    private MultipartFile profileImage;
}
