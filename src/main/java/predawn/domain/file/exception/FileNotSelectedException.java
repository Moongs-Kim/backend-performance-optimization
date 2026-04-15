package predawn.domain.file.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class FileNotSelectedException extends BusinessException {
    public FileNotSelectedException() {
        super(ErrorCode.FILE_NOT_SELECTED);
    }
}
