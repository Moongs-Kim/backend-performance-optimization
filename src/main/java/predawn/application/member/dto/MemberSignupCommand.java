package predawn.application.member.dto;

import lombok.Getter;
import predawn.domain.member.enums.Gender;

import java.time.LocalDate;

@Getter
public class MemberSignupCommand {

    private String loginId;
    private String password;
    private String name;
    private LocalDate birthDate;
    private Gender gender;
    private String email;
    private String address;

    public MemberSignupCommand(
            String loginId, String password, String name, LocalDate birthDate,
           Gender gender, String email, String address)
    {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.email = email;
        this.address = address;
    }
}
