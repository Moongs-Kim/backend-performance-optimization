package predawn.domain.board.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class BoardAccessDeniedException extends BusinessException {
    public BoardAccessDeniedException() {
        super(ErrorCode.BOARD_ACCESS_DENIED);
    }
}
