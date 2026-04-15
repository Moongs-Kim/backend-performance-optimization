package predawn.domain.file.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class NotAllowedFileException extends BusinessException {
    public NotAllowedFileException() {
        super(ErrorCode.NOT_ALLOWED_FILE);
    }
}
