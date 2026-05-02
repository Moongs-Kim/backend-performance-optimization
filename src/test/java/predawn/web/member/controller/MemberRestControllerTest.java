package predawn.web.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import predawn.application.member.dto.PasswordChangeCommand;
import predawn.application.member.service.MemberService;
import predawn.domain.member.enums.FriendStatus;
import predawn.domain.member.exception.MemberNotFoundException;
import predawn.domain.member.repository.MemberRepository;
import predawn.global.filter.limiter.RequestLimiter;
import predawn.web.member.dto.FindLoginIdRequest;
import predawn.web.member.dto.PasswordResetCheckRequest;
import predawn.web.member.dto.PasswordResetRequest;
import predawn.web.member.session.LoginMember;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static predawn.web.member.session.SessionConst.LOGIN_MEMBER;

@WebMvcTest(controllers = MemberRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberRestControllerTest {

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

    @DisplayName("이메일로 회원의 로그인 ID를 조회할 수 있다")
    @Test
    void findMemberLoginId() throws Exception {
        //Given
        FindLoginIdRequest findLoginIdRequest = FindLoginIdRequest.builder()
                .email("test@test.com")
                .build();

        given(memberService.findMemberLoginId(anyString()))
                .willReturn("user1");

        //When //Then
        mockMvc.perform(post("/api/member/find-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(findLoginIdRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.value").value("user1"));

        then(memberService)
                .should()
                .findMemberLoginId(anyString());
    }

    @DisplayName("존재하지 않는 이메일로 회원을 조회할 수 없다")
    @Test
    void findMemberLoginId_NotFound() throws Exception {
        //Given
        FindLoginIdRequest findLoginIdRequest = FindLoginIdRequest.builder()
                .email("email@notExists.com")
                .build();

        given(memberService.findMemberLoginId(anyString()))
                .willThrow(new MemberNotFoundException());

        //When //Then
        mockMvc.perform(post("/api/member/find-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(findLoginIdRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("MEMBER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("회원 정보를 찾을 수 없습니다"));

        then(memberService)
                .should(times(1))
                .findMemberLoginId(anyString());
    }

    @DisplayName("회원 로그인 ID 조회시 이메일을 필수로 입력해야 한다")
    @Test
    void findMemberLoginId_validationFail_byEmptyEmail() throws Exception {
        //Given
        FindLoginIdRequest findLoginIdRequest = FindLoginIdRequest.builder()
                .email("")
                .build();

        //When //Then
        mockMvc.perform(post("/api/member/find-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(findLoginIdRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("이메일을 입력해 주세요"));;

        then(memberService)
                .should(never())
                .findMemberLoginId(anyString());
    }

    @DisplayName("회원 로그인 ID 조회시 이메일 형식이 맞아야 한다")
    @Test
    void findMemberLoginId_validationFail_byInvalidEmail() throws Exception {
        //Given
        FindLoginIdRequest findLoginIdRequest = FindLoginIdRequest.builder()
                .email("email.com")
                .build();

        //When //Then
        mockMvc.perform(post("/api/member/find-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(findLoginIdRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("유효하지 않은 이메일 형식 입니다"));;

        then(memberService)
                .should(never())
                .findMemberLoginId(anyString());
    }

    @DisplayName("로그인 ID와 이메일을 입력하면 비밀번호 변경 토큰을 쿠키로 응답 받는다")
    @Test
    void createPasswordResetToken() throws Exception {
        //Given
        PasswordResetCheckRequest passwordResetCheckRequest = PasswordResetCheckRequest.builder()
                .loginId("login-id")
                .email("test@test.com")
                .build();

        given(memberService.createPasswordResetToken(anyString(), anyString()))
                .willReturn(UUID.randomUUID().toString());

        //When //Then
        mockMvc.perform(post("/api/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetCheckRequest))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().exists("password_reset_token"));

        then(memberService)
                .should(times(1))
                .createPasswordResetToken(anyString(), anyString());
    }

    @DisplayName("로그인 ID를 입력하지 않으면 검증 오류가 발생한다")
    @Test
    void createPasswordResetToken_EmptyLoginId() throws Exception {
        //Given
        PasswordResetCheckRequest passwordResetCheckRequest = PasswordResetCheckRequest.builder()
                .loginId("")
                .email("test@test.com")
                .build();

        //When
        mockMvc.perform(post("/api/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetCheckRequest))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors[0].field").value("loginId"))
                .andExpect(jsonPath("$.errors[0].message").value("로그인 아이디를 입력해 주세요"));

        then(memberService)
                .should(never())
                .createPasswordResetToken(anyString(), anyString());
    }

    @DisplayName("이메일를 입력하지 않으면 검증 오류가 발생한다")
    @Test
    void createPasswordResetToken_EmptyEmail() throws Exception {
        //Given
        PasswordResetCheckRequest passwordResetCheckRequest = PasswordResetCheckRequest.builder()
                .loginId("login-id")
                .email("")
                .build();

        //When
        mockMvc.perform(post("/api/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetCheckRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("이메일을 입력해 주세요"));

        then(memberService)
                .should(never())
                .createPasswordResetToken(anyString(), anyString());
    }

    @DisplayName("이메일 형식이 올바르지 않으면 검증 오류가 발생한다")
    @Test
    void createPasswordResetToken_InvalidEmail() throws Exception {
        //Given
        PasswordResetCheckRequest passwordResetCheckRequest = PasswordResetCheckRequest.builder()
                .loginId("login-id")
                .email("invalid-email")
                .build();

        //When
        mockMvc.perform(post("/api/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetCheckRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("유효하지 않은 이메일 형식 입니다"));

        then(memberService)
                .should(never())
                .createPasswordResetToken(anyString(), anyString());
    }

    @DisplayName("비밀번호 변경 후 인증에 사용된 쿠키는 사라진다")
    @Test
    void passwordChange() throws Exception {
        //Given
        Cookie cookie = new Cookie("password_reset_token", UUID.randomUUID().toString());
        cookie.setMaxAge(300);

        PasswordResetRequest passwordResetRequest = PasswordResetRequest.builder()
                .newPassword("4321")
                .passwordCheck("4321")
                .build();

        willDoNothing().given(memberService).passwordChange(any(PasswordChangeCommand.class));

        //When //Then
        mockMvc.perform(post("/api/password-reset/confirm")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetRequest))
                )
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("password_reset_token", 0));

        then(memberService)
                .should(times(1))
                .passwordChange(any(PasswordChangeCommand.class));
    }

    @DisplayName("인증 후 받은 쿠키 없이 요청을 보내면 예외가 발생한다")
    @Test
    void passwordChange_EmptyCookie() throws Exception {
        //Given
        PasswordResetRequest passwordResetRequest = PasswordResetRequest.builder()
                .newPassword("4321")
                .passwordCheck("4321")
                .build();

        willDoNothing().given(memberService).passwordChange(any(PasswordChangeCommand.class));

        //When //Then
        mockMvc.perform(post("/api/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("MISSING_COOKIE"))
                .andExpect(jsonPath("$.message").value("인증을 먼저 진행해 주세요"));

        then(memberService)
                .should(never())
                .passwordChange(any(PasswordChangeCommand.class));
    }

    @DisplayName("변경할 비밀번호를 입력하지 않으면 검증 오류가 발생한다")
    @Test
    void passwordChange_EmptyPassword() throws Exception {
        //Given
        Cookie cookie = new Cookie("password_reset_token", UUID.randomUUID().toString());
        cookie.setMaxAge(300);

        PasswordResetRequest passwordResetRequest = PasswordResetRequest.builder()
                .newPassword("")
                .passwordCheck("4321")
                .build();

        willDoNothing().given(memberService).passwordChange(any(PasswordChangeCommand.class));

        //When //Then
        mockMvc.perform(post("/api/password-reset/confirm")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors[0].field").value("newPassword"))
                .andExpect(jsonPath("$.errors[0].message").value("비밀번호를 입력해 주세요"));

        then(memberService)
                .should(never())
                .passwordChange(any(PasswordChangeCommand.class));
    }

    @DisplayName("비밀번호 확인을 입력하지 않으면 검증 오류가 발생한다")
    @Test
    void passwordChange_EmptyPasswordCheck() throws Exception {
        //Given
        Cookie cookie = new Cookie("password_reset_token", UUID.randomUUID().toString());
        cookie.setMaxAge(300);

        PasswordResetRequest passwordResetRequest = PasswordResetRequest.builder()
                .newPassword("4321")
                .passwordCheck("")
                .build();

        willDoNothing().given(memberService).passwordChange(any(PasswordChangeCommand.class));

        //When //Then
        mockMvc.perform(post("/api/password-reset/confirm")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors[0].field").value("passwordCheck"))
                .andExpect(jsonPath("$.errors[0].message").value("비밀번호 확인을 입력해 주세요"));

        then(memberService)
                .should(never())
                .passwordChange(any(PasswordChangeCommand.class));
    }

    @DisplayName("비밀번호와 비밀번호 확인이 다르면 검증 오류가 발생한다")
    @Test
    void passwordChange_NotSamePassword() throws Exception {
        //Given
        Cookie cookie = new Cookie("password_reset_token", UUID.randomUUID().toString());
        cookie.setMaxAge(300);

        PasswordResetRequest passwordResetRequest = PasswordResetRequest.builder()
                .newPassword("4321")
                .passwordCheck("1234")
                .build();

        willDoNothing().given(memberService).passwordChange(any(PasswordChangeCommand.class));

        //When //Then
        mockMvc.perform(post("/api/password-reset/confirm")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("PASSWORD_NOT_MATCH"))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다"));

        then(memberService)
                .should(never())
                .passwordChange(any(PasswordChangeCommand.class));
    }

    @DisplayName("")
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
}