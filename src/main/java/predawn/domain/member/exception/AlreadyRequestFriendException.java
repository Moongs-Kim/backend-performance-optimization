package predawn.domain.member.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class AlreadyRequestFriendException extends BusinessException {
    public AlreadyRequestFriendException() {
        super(ErrorCode.ALREADY_REQUEST_FRIEND);
    }
}
