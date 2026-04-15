package predawn.web.board.mapper;

import predawn.application.board.dto.BoardPostCommand;
import predawn.application.board.dto.BoardSearchCond;
import predawn.application.board.dto.BoardUpdateCommand;
import predawn.domain.board.enums.BoardOpen;
import predawn.domain.board.enums.CategoryName;
import predawn.domain.common.exception.BadRequestException;
import predawn.web.board.dto.BoardPostForm;
import predawn.web.board.dto.BoardUpdateReqDto;
import predawn.web.board.dto.BoardsReqDto;

public abstract class BoardCommandMapper {

    public static BoardPostCommand toPostCommand(BoardPostForm postForm) {
        return new BoardPostCommand(
                postForm.getTitle(),
                postForm.getContent(),
                postForm.getBoardOpen(),
                postForm.getCategoryName()
        );
    }

    public static BoardUpdateCommand toUpdateCommand(Long boardId, BoardUpdateReqDto boardUpdateDto) {
        try {
            return new BoardUpdateCommand(
                    boardId,
                    CategoryName.valueOf(boardUpdateDto.getCategoryName()),
                    BoardOpen.valueOf(boardUpdateDto.getBoardOpen()),
                    boardUpdateDto.getTitle(),
                    boardUpdateDto.getContent()
            );
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e);
        }
    }

    public static BoardSearchCond toSearchCond(BoardsReqDto boardsReqDto) {
        return BoardSearchCond.of(
                boardsReqDto.getSearchType(),
                boardsReqDto.getKeyword(),
                boardsReqDto.getSortType()
        );
    }
}
