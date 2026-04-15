package predawn.global.pagination;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class PageBadRequestException extends BusinessException {
    public PageBadRequestException() {
        super(ErrorCode.PAGE_BAD_REQUEST);
    }
}
