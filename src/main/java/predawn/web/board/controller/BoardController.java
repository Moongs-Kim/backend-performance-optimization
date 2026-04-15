package predawn.web.board.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttribute;
import predawn.application.board.dto.BoardDetailQueryDto;
import predawn.application.board.dto.BoardListQueryDto;
import predawn.application.board.dto.BoardSearchCond;
import predawn.application.board.enums.SortType;
import predawn.application.board.service.BoardService;
import predawn.domain.board.entity.Board;
import predawn.domain.board.enums.BoardOpen;
import predawn.domain.board.enums.CategoryName;
import predawn.global.pagination.PageInformation;
import predawn.global.pagination.Paging;
import predawn.web.board.dto.BoardPostForm;
import predawn.web.board.dto.BoardUpdateResDto;
import predawn.web.board.dto.BoardsReqDto;
import predawn.web.board.mapper.BoardCommandMapper;
import predawn.web.member.session.LoginMember;

import java.util.List;

import static predawn.web.member.session.SessionConst.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/board/write")
    public String boardWritePage(Model model) {
        model.addAttribute("postForm", new BoardPostForm());
        return "board/board-write";
    }

    @GetMapping("/boards")
    public String boardListPage(
            Model model,
            @ModelAttribute BoardsReqDto boardsReqDto)
    {
        BoardSearchCond boardSearchCond = BoardCommandMapper.toSearchCond(boardsReqDto);

        PageInformation pageInformation =
                (boardsReqDto.getPageIndex() == null)
                        ? new PageInformation()
                        : new PageInformation(boardsReqDto.getPageIndex());

        Page<BoardListQueryDto> boardPageDto = getBoards(boardSearchCond, pageInformation);

        Paging paging = new Paging(boardPageDto);

        List<BoardListQueryDto> boardListDto = boardPageDto.getContent();

        model.addAttribute("boardList", boardListDto);
        model.addAttribute("paging", paging);
        return "board/board-list";
    }

    @GetMapping("/board/{boardId}")
    public String boardDetailPage(
            Model model,
            @PathVariable Long boardId,
            @SessionAttribute(name = LOGIN_MEMBER) LoginMember loginMember)
    {
        int commentPageSize = 3;
        BoardDetailQueryDto boardDetailDto = boardService.getBoardDetail(boardId, loginMember.getId(), commentPageSize);

        model.addAttribute("boardDetail", boardDetailDto);
        model.addAttribute("isReaderSameWriter", boardDetailDto.getWriterId().equals(loginMember.getId()));
        return "board/board-detail";
    }

    @GetMapping("/board/{boardId}/update")
    public String boardUpdatePage(
            Model model,
            @PathVariable Long boardId,
            @SessionAttribute(name = LOGIN_MEMBER) LoginMember loginMember)
    {
        Board board = boardService.getBoardDetailForUpdate(boardId, loginMember.getId());

        model.addAttribute("board", new BoardUpdateResDto(board));

        return "board/board-update";
    }

    @ModelAttribute("categories")
    private CategoryName[] categories() {
        return CategoryName.values();
    }

    @ModelAttribute("boardOpens")
    private BoardOpen[] boardOpens() {
        return BoardOpen.values();
    }

    private Page<BoardListQueryDto> getBoards(BoardSearchCond boardSearchCond, PageInformation pageInformation) {
        SortType sortType = boardSearchCond.getSortType();
        return sortType.isTopNSort()
                ? boardService.getBoardListTopN(boardSearchCond, pageInformation)
                : boardService.getBoardList(boardSearchCond, pageInformation);
    }
}
