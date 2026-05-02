package predawn.web.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import predawn.application.member.service.MemberService;
import predawn.domain.common.exception.BadRequestException;
import predawn.domain.member.enums.FriendStatus;
import predawn.domain.member.repository.MemberRepository;
import predawn.global.filter.limiter.RequestLimiter;
import predawn.web.member.session.LoginMember;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static predawn.web.member.session.SessionConst.LOGIN_MEMBER;

@WebMvcTest(controllers = MemberRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class FriendMemberRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private RequestLimiter requestLimiter;

    @DisplayName("친구 요청이 성공하면 상태코드 200과 성공 메시지가 응답된다")
    @Test
    void requestFriend() throws Exception {
        //Given
        given(memberService.requestFriend(anyLong(), anyString()))
                .willReturn(FriendStatus.REQUESTED);

        //When //Then
        mockMvc.perform(post("/api/friend/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("loginId"))
                .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.value").value("친구 요청을 보냈습니다"));

        then(memberService)
                .should(times(1))
                .requestFriend(anyLong(), anyString());
    }

    @DisplayName("친구 요청에서 차단 상태이면 상태코드 200과 차단 되었다는 메시지가 응답된다")
    @Test
    void requestFriend_ByFriendStatusBlock() throws Exception {
        //Given
        given(memberService.requestFriend(anyLong(), anyString()))
                .willReturn(FriendStatus.BLOCKED);

        //When //Then
        mockMvc.perform(post("/api/friend/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("loginId"))
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.value").value("친구 요청이 차단되어 있습니다"));

        then(memberService)
                .should(times(1))
                .requestFriend(anyLong(), anyString());
    }

    @DisplayName("친구 요청에서 승인 상태이면 상태코드 200과 이미 친구라는 메시지가 응답된다")
    @Test
    void requestFriend_ByFriendStatusAccept() throws Exception {
        //Given
        given(memberService.requestFriend(anyLong(), anyString()))
                .willReturn(FriendStatus.ACCEPTED);

        //When //Then
        mockMvc.perform(post("/api/friend/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("loginId"))
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.value").value("이미 친구인 상태 입니다"));

        then(memberService)
                .should(times(1))
                .requestFriend(anyLong(), anyString());
    }

    @DisplayName("친구 요청에서 친구 로그인 ID를 입력하지 않으면 예외가 발생한다")
    @Test
    void requestFriend_EmptyLoginId() throws Exception {
        //Given //When //Then
        mockMvc.perform(post("/api/friend/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALUE_REQUIRED"))
                .andExpect(jsonPath("$.message").value("필수 값을 입력해 주세요"));
    }

    @DisplayName("요청하는 본인의 로그인 ID로 친구 요청을 보내면 예외가 발생한다")
    @Test
    void requestFriend_FailBySameLoginId() throws Exception {
        //Given
        given(memberService.requestFriend(anyLong(), anyString()))
                .willThrow(new BadRequestException());

        //When
        mockMvc.perform(post("/api/friend/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("sameLoginId"))
                        .sessionAttr(LOGIN_MEMBER, new LoginMember(1L, "user", null))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"));

        then(memberService)
                .should(times(1))
                .requestFriend(anyLong(), anyString());

    }
}