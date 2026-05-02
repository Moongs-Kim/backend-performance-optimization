package predawn.domain.member.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class AuthenticationFailException extends BusinessException {
    public AuthenticationFailException() {
        super(ErrorCode.AUTHENTICATION_FAIL);
    }
}
