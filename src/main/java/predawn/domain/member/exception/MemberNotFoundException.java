package predawn.domain.member.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class MemberNotFoundException extends BusinessException {
    public MemberNotFoundException() {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }
}
