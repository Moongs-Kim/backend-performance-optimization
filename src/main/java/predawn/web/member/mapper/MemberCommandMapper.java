package predawn.web.member.mapper;

import predawn.application.member.dto.MemberSignupCommand;
import predawn.web.member.dto.MemberSignupForm;

public abstract class MemberCommandMapper {

    public static MemberSignupCommand toSignupCommand(MemberSignupForm signupForm) {
        return new MemberSignupCommand(
                signupForm.getLoginId(),
                signupForm.getPassword(),
                signupForm.getName(),
                signupForm.getBirthDate(),
                signupForm.getGender(),
                signupForm.getEmail(),
                signupForm.getAddress()
        );
    }
}
