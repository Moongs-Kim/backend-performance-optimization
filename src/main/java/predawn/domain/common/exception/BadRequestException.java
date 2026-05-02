package predawn.domain.common.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class BadRequestException extends BusinessException {
    public BadRequestException(Throwable cause) {
        super(ErrorCode.BAD_REQUEST, cause);
    }

    public BadRequestException() {
        super(ErrorCode.BAD_REQUEST);
    }
}
