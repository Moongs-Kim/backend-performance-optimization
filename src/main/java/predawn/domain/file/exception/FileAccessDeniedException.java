package predawn.domain.file.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class FileAccessDeniedException extends BusinessException {
    public FileAccessDeniedException() {
        super(ErrorCode.FILE_ACCESS_DENIED);
    }
}
