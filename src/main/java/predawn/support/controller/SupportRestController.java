package predawn.support.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import predawn.application.board.dto.BoardListQueryDto;
import predawn.application.board.dto.BoardSearchCond;
import predawn.application.board.enums.SortType;
import predawn.application.board.service.BoardService;
import predawn.global.pagination.PageInformation;
import predawn.global.pagination.Paging;
import predawn.web.board.dto.BoardsReqDto;
import predawn.web.board.mapper.BoardCommandMapper;
import predawn.web.common.ApiResponse;

import java.util.List;

@RestController
@Profile({"dev", "load"})
@RequiredArgsConstructor
public class SupportRestController {

    private final BoardService boardService;

    @GetMapping("/dev/api/boards")
    public ResponseEntity<ApiResponse<?>> boardList(
            @ModelAttribute BoardsReqDto boardsReqDto
    ) {
        BoardSearchCond boardSearchCond = BoardCommandMapper.toSearchCond(boardsReqDto);

        PageInformation pageInformation =
                (boardsReqDto.getPageIndex() == null)
                        ? new PageInformation()
                        : new PageInformation(boardsReqDto.getPageIndex());

        Page<BoardListQueryDto> boardPageDto = getBoards(boardSearchCond, pageInformation);

        Paging paging = new Paging(boardPageDto);

        List<BoardListQueryDto> boardListDto = boardPageDto.getContent();

        BoardListResponse boardListResponse = new BoardListResponse(boardListDto, paging);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), boardListResponse));
    }

    private Page<BoardListQueryDto> getBoards(BoardSearchCond boardSearchCond, PageInformation pageInformation) {
        SortType sortType = boardSearchCond.getSortType();
        return sortType.isTopNSort()
                ? boardService.getBoardListTopN(boardSearchCond, pageInformation)
                : boardService.getBoardList(boardSearchCond, pageInformation);
    }

    @Getter
    static class BoardListResponse {
        private final List<BoardListQueryDto> boardList;
        private final Paging paging;

        public BoardListResponse(List<BoardListQueryDto> boardList, Paging paging) {
            this.boardList = boardList;
            this.paging = paging;
        }
    }

}
