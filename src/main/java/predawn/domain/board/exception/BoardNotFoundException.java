package predawn.domain.board.exception;

import predawn.global.error.ErrorCode;
import predawn.global.error.exception.BusinessException;

public class BoardNotFoundException extends BusinessException {
    public BoardNotFoundException() {
        super(ErrorCode.BOARD_NOT_FOUND);
    }
}
