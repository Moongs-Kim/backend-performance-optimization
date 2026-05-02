package predawn.domain.member.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class PasswordEmptyException extends BusinessException {
    public PasswordEmptyException() {
        super(ErrorCode.PASSWORD_EMPTY);
    }
}
