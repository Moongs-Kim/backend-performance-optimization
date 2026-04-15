package predawn.domain.file.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class FileNotFoundException extends BusinessException {
    public FileNotFoundException() {
        super(ErrorCode.FILE_NOT_FOUND);
    }
}
