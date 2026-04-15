package predawn.application.member.factory;

import predawn.application.member.dto.MemberSignupCommand;
import predawn.domain.file.entity.UploadFile;
import predawn.domain.member.entity.Member;

public abstract class MemberFactory {

    public static Member create(MemberSignupCommand signupCommand, UploadFile profileImage) {
        return new Member(
                signupCommand.getLoginId(),
                signupCommand.getPassword(),
                signupCommand.getName(),
                signupCommand.getBirthDate(),
                signupCommand.getGender(),
                signupCommand.getEmail(),
                signupCommand.getAddress(),
                profileImage
        );
    }
}
