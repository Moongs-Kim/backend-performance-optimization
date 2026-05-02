package predawn.global.error.exception;

import predawn.global.error.ErrorCode;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public BusinessException(Throwable cause) {
        this(ErrorCode.INTERNAL_SERVER_ERROR, cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
