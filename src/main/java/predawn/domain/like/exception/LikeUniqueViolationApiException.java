package predawn.domain.like.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class LikeUniqueViolationApiException extends BusinessException {

    public LikeUniqueViolationApiException(Throwable cause) {
        super(ErrorCode.LIKE_UNIQUE_VIOLATION, cause);
    }
}
