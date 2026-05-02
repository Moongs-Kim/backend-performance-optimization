package predawn.web.board.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import predawn.application.board.service.BoardService;
import predawn.global.filter.limiter.RequestLimiter;
import predawn.infrastructure.redis.BoardRedisRepository;
import predawn.web.member.session.LoginMember;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static predawn.web.member.session.SessionConst.LOGIN_MEMBER;

@WebMvcTest(controllers = BoardRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class BoardRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardService boardService;

    @MockitoBean
    private BoardRedisRepository boardRedisRepository;

    @MockitoBean
    private RequestLimiter requestLimiter;

    @DisplayName("첨부파일 없이 게시글을 등록한다")
    @Test
    void boardWrite() throws Exception {
        //Given
        given(boardService.postBoard(any(), any(), any()))
                .willReturn(1L);

        //When
        mockMvc.perform(post("/api/board/write")
                        .param("title", "게시글 제목")
                        .param("content", "게시글 내용")
                        .param("boardOpen", "ALL")
                        .param("categoryName", "GENERAL")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/boards/1"));

        then(boardService)
                .should()
                .postBoard(eq(1L), any(), any());
    }

    @DisplayName("첨부파일과 같이 게시글을 등록한다")
    @Test
    void boardWrite_withFiles() throws Exception {
        //Given
        MockMultipartFile attachFiles1 = new MockMultipartFile(
                "attachFiles",
                "test.jpg",
                "image/jpeg",
                "dummy-image".getBytes()
        );

        MockMultipartFile attachFiles2 = new MockMultipartFile(
                "attachFiles",
                "test.txt",
                "text/plain",
                "dummy-text".getBytes()
        );

        given(boardService.postBoard(any(), any(), any()))
                .willReturn(1L);

        //When
        mockMvc.perform(multipart("/api/board/write")
                        .file(attachFiles1)
                        .file(attachFiles2)
                        .param("title", "게시글 제목")
                        .param("content", "게시글 내용")
                        .param("boardOpen", "ALL")
                        .param("categoryName", "GENERAL")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isCreated());

        then(boardService)
                .should()
                .postBoard(eq(1L), any(), any());
    }

    @DisplayName("로그인 사용자가 아니면 게시글을 등록할 수 없고 로그인 페이지로 리다이렉트 된다")
    @Test
    void boardWrite_NotLogin() throws Exception {
        //Given //When //Then
        mockMvc.perform(post("/api/board/write")
                        .param("title", "게시글 제목")
                        .param("content", "게시글 내용")
                        .param("boardOpen", "ALL")
                        .param("categoryName", "GENERAL")
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?redirectURL=/api/board/write"));

        then(boardService)
                .should(never())
                .postBoard(any(), any(), any());
    }

    @DisplayName("게시글을 등록할때 제목을 필수로 작성해야 한다")
    @Test
    void boardWrite_validationFail_byEmptyTitle() throws Exception {
        //Given //When //Then
        mockMvc.perform(post("/api/board/write")
                        .param("title", "")
                        .param("content", "게시글 내용")
                        .param("boardOpen", "ALL")
                        .param("categoryName", "GENERAL")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].message").value("제목을 입력해 주세요"));

        then(boardService)
                .should(never())
                .postBoard(any(), any(), any());
    }

    @DisplayName("게시글을 등록할때 내용을 필수로 작성해야 한다")
    @Test
    void boardWrite_validationFail_byEmptyContent() throws Exception {
        //Given //When //Then
        mockMvc.perform(post("/api/board/write")
                        .param("title", "게시글 제목")
                        .param("content", "")
                        .param("boardOpen", "ALL")
                        .param("categoryName", "GENERAL")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("content"))
                .andExpect(jsonPath("$.errors[0].message").value("내용을 입력해 주세요"));

        then(boardService)
                .should(never())
                .postBoard(any(), any(), any());
    }

    @DisplayName("게시글을 등록할때 공개 여부를 필수로 선택해야 한다")
    @Test
    void boardWrite_validationFail_byEmptyBoardOpen() throws Exception {
        //Given //When //Then
        mockMvc.perform(post("/api/board/write")
                        .param("title", "게시글 제목")
                        .param("content", "게시글 내용")
                        .param("boardOpen", "")
                        .param("categoryName", "GENERAL")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("boardOpen"))
                .andExpect(jsonPath("$.errors[0].message").value("공개 여부를 선택해 주세요"));

        then(boardService)
                .should(never())
                .postBoard(any(), any(), any());
    }

    @DisplayName("게시글을 등록할때 공개 여부를 필수로 선택해야 한다")
    @Test
    void boardWrite_validationFail_byEmptyCategoryName() throws Exception {
        //Given //When //Then
        mockMvc.perform(post("/api/board/write")
                        .param("title", "게시글 제목")
                        .param("content", "게시글 내용")
                        .param("boardOpen", "ALL")
                        .param("categoryName", "")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("categoryName"))
                .andExpect(jsonPath("$.errors[0].message").value("카테고리를 선택해 주세요"));

        then(boardService)
                .should(never())
                .postBoard(any(), any(), any());
    }

    @DisplayName("게시글 조회 기록이 남아있지 않으면 조회수 증가 메소드가 실행된다")
    @Test
    void increaseViewCount() throws Exception {
        //Given
        given(boardRedisRepository.saveViewIfNotExists(anyLong(), anyLong()))
                .willReturn(true);

        willDoNothing().given(boardService).incrementViewCount(anyLong(), anyLong());

        //When //Then
        mockMvc.perform(post("/api/board/1/view")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isOk());

        then(boardService)
                .should(times(1))
                .incrementViewCount(anyLong(), anyLong());
    }

    @DisplayName("게시글 조회 기록이 남아있으면 조회수 증가 메소드가 실행되지 않는다")
    @Test
    void increaseViewCount_Fail() throws Exception {
        //Given
        given(boardRedisRepository.saveViewIfNotExists(anyLong(), anyLong()))
                .willReturn(false);

        willDoNothing().given(boardService).incrementViewCount(anyLong(), anyLong());

        //When //Then
        mockMvc.perform(post("/api/board/1/view")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isOk());

        then(boardService)
                .should(never())
                .incrementViewCount(anyLong(), anyLong());
    }

    @DisplayName("URL에 잘못된 게시글 ID를 입력하면 ")
    @Test
    void increaseViewCount_FailByWrongURL() throws Exception {
        //Given //When //Then
        mockMvc.perform(post("/api/board/NOT-NUMBER/view")
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentTypeMismatchException));
    }
}