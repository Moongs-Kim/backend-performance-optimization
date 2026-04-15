package predawn.web.member.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import predawn.web.member.dto.MemberSignupForm;

@RequiredArgsConstructor
public class SignupValidator {

    private final MemberSignupForm signupForm;
    private final BindingResult bindingResult;

    public void validate() {
        idDuplicationCheck();
        passwordCheck();
    }

    private void idDuplicationCheck() {
        if (StringUtils.hasText(signupForm.getLoginId()) && !signupForm.isIdDuplicationCheck()) {
            bindingResult.rejectValue("idDuplicationCheck", "required");
        }
    }

    private void passwordCheck() {
        String password = signupForm.getPassword();
        String passwordCheck = signupForm.getPasswordCheck();
        if (StringUtils.hasText(password) && StringUtils.hasText(passwordCheck)) {
            if (!password.equals(passwordCheck)) {
                bindingResult.reject("not-match.password");
            }
        }
    }
}
