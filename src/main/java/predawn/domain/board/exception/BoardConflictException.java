package predawn.domain.board.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class BoardConflictException extends BusinessException {
    public BoardConflictException(Throwable cause) {
        super(ErrorCode.BOARD_CONFLICT, cause);
    }
}
