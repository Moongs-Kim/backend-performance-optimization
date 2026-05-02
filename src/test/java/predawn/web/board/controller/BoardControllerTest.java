package predawn.web.board.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import predawn.application.board.dto.BoardListQueryDto;
import predawn.application.board.dto.BoardSearchCond;
import predawn.application.board.service.BoardService;
import predawn.global.filter.limiter.RequestLimiter;
import predawn.global.pagination.PageInformation;
import predawn.web.member.session.LoginMember;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static predawn.web.member.session.SessionConst.LOGIN_MEMBER;

@WebMvcTest(controllers = BoardController.class)
@AutoConfigureMockMvc(addFilters = false)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardService boardService;

    @MockitoBean
    private RequestLimiter requestLimiter;

    @DisplayName("게시글 작성 페이지를 가져온다")
    @Test
    void boardWritePage() throws Exception {
        //Given //When //Then
        mockMvc.perform(get("/board/write")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postForm"))
                .andExpect(view().name("board/board-write"));
    }

    @DisplayName("로그인 사용자가 아니면 게시글을 등록 페이지를 가져올 수 없고 로그인 페이지로 리다이렉트 된다")
    @Test
    void boardWritePage_NotLogin() throws Exception {
        //Given //When //Then
        mockMvc.perform(get("/board/write"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?redirectURL=/board/write"));
    }

    @DisplayName("sortType이 latest이면 BoardService의 getBoardList 메서드를 호출한다")
    @Test
    void boardListPage_byLatest() throws Exception {
        //Given
        PageImpl<BoardListQueryDto> boardListQueryDtos = getBoardListQueryDtos();

        given(boardService.getBoardList(any(BoardSearchCond.class), any(PageInformation.class)))
                .willReturn(boardListQueryDtos);

        //When
        mockMvc.perform(get("/boards")
                        .param("searchType", "")
                        .param("keyword", "")
                        .param("sortType", "latest")
                        .param("pageIndex", "0")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("boardList"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(view().name("board/board-list"));

        then(boardService).should(times(1)).getBoardList(any(BoardSearchCond.class), any(PageInformation.class));
        then(boardService).should(never()).getBoardListTopN(any(BoardSearchCond.class), any(PageInformation.class));
    }

    @DisplayName("sortType이 view_count BoardService의 getBoardList 메서드를 호출한다")
    @Test
    void boardListPage_byViewCount() throws Exception {
        //Given
        PageImpl<BoardListQueryDto> boardListQueryDtos = getBoardListQueryDtos();

        given(boardService.getBoardList(any(BoardSearchCond.class), any(PageInformation.class)))
                .willReturn(boardListQueryDtos);

        //When
        mockMvc.perform(get("/boards")
                        .param("searchType", "")
                        .param("keyword", "")
                        .param("sortType", "view_count")
                        .param("pageIndex", "0")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("board/board-list"))
                .andExpect(model().attributeExists("boardList"))
                .andExpect(model().attributeExists("paging"));

        then(boardService).should(times(1)).getBoardList(any(BoardSearchCond.class), any(PageInformation.class));
        then(boardService).should(never()).getBoardListTopN(any(BoardSearchCond.class), any(PageInformation.class));
    }

    @DisplayName("sortType이 latest_top_n_like_count이면 BoardService의 getBoardListTopN 메서드를 호출한다")
    @Test
    void boardListPage_byLatestTopNLikeCount() throws Exception {
        //Given
        PageImpl<BoardListQueryDto> boardListQueryDtos = getBoardListQueryDtos();

        given(boardService.getBoardListTopN(any(BoardSearchCond.class), any(PageInformation.class)))
                .willReturn(boardListQueryDtos);

        //When
        mockMvc.perform(get("/boards")
                        .param("searchType", "")
                        .param("keyword", "")
                        .param("sortType", "latest_top_n_like_count")
                        .param("pageIndex", "0")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("board/board-list"))
                .andExpect(model().attributeExists("boardList"))
                .andExpect(model().attributeExists("paging"));

        then(boardService).should(never()).getBoardList(any(BoardSearchCond.class), any(PageInformation.class));
        then(boardService).should(times(1)).getBoardListTopN(any(BoardSearchCond.class), any(PageInformation.class));
    }

    private PageImpl<BoardListQueryDto> getBoardListQueryDtos() {
        BoardListQueryDto boardListQueryDto1 = new BoardListQueryDto(1L, "제목1", 10, LocalDateTime.now(), "user1", 10L);
        BoardListQueryDto boardListQueryDto2 = new BoardListQueryDto(2L, "제목2", 10, LocalDateTime.now(), "user2", 10L);

        return new PageImpl<>(List.of(boardListQueryDto1, boardListQueryDto2));
    }
}