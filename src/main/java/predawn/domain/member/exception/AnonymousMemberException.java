package predawn.domain.member.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class AnonymousMemberException extends BusinessException {
    public AnonymousMemberException() {
        super(ErrorCode.ANONYMOUS_MEMBER);
    }
}
