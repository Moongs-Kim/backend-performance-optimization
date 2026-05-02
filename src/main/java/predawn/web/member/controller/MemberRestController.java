package predawn.web.member.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.*;
import predawn.application.member.dto.PasswordChangeCommand;
import predawn.application.member.service.MemberService;
import predawn.domain.member.enums.FriendStatus;
import predawn.domain.member.repository.MemberRepository;
import predawn.global.error.ErrorResponse;
import predawn.global.error.exception.ValidationException;
import predawn.web.common.ApiResponse;
import predawn.web.member.dto.FindLoginIdRequest;
import predawn.web.member.dto.PasswordResetCheckRequest;
import predawn.web.member.dto.PasswordResetRequest;
import predawn.web.member.session.LoginMember;

import static predawn.web.member.session.SessionConst.LOGIN_MEMBER;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberRestController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestCookie(MissingRequestCookieException e) {
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST, "MISSING_COOKIE", "인증을 먼저 진행해 주세요"
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST, "VALUE_REQUIRED", "필수 값을 입력해 주세요"
                ));
    }

    @PostMapping("/api/signup/id-check")
    public ResponseEntity<Boolean> idDuplicationCheck(@RequestBody String checkId) {
        if (!StringUtils.hasText(checkId)) {
            return ResponseEntity
                    .badRequest()
                    .body(false);
        }

        boolean idDuplicationCheckResult = memberRepository.findMemberByLoginId(checkId) == null;

        return ResponseEntity.ok(idDuplicationCheckResult);
    }

    @PostMapping("/api/member/find-id")
    public ResponseEntity<ApiResponse<String>> findMemberLoginId(
            @Valid @RequestBody FindLoginIdRequest findLoginIdRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult, "Member findMemberLoginId Validation Failed");

        String memberLoginId = memberService.findMemberLoginId(findLoginIdRequest.getEmail());

        return ResponseEntity
                .ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), memberLoginId));
    }

    @PostMapping("/api/password-reset/request")
    public ResponseEntity<?> createPasswordResetToken(
            @Valid @RequestBody PasswordResetCheckRequest passwordResetCheckRequest,
            BindingResult bindingResult,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult, "Member createPasswordResetToken Validation Failed");

        String token = memberService.createPasswordResetToken(
                passwordResetCheckRequest.getLoginId(), passwordResetCheckRequest.getEmail()
        );

        Cookie cookie = createPasswordResetCookie(token);

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/password-reset/confirm")
    public ResponseEntity<?> passwordChange(
            @Valid @RequestBody PasswordResetRequest passwordResetRequest,
            BindingResult bindingResult,
            @CookieValue("password_reset_token") String passwordResetToken,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) throw new ValidationException(bindingResult, "Member passwordChange Validation Failed");

        String newPassword = passwordResetRequest.getNewPassword();
        String passwordCheck = passwordResetRequest.getPasswordCheck();

        if (isNotSame(newPassword, passwordCheck)) return responsePasswordNotMatch();

        PasswordChangeCommand passwordChangeCommand = PasswordChangeCommand.of(passwordResetToken, newPassword);

        memberService.passwordChange(passwordChangeCommand);

        Cookie cookie = deletePasswordResetCookie();

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/friend/request")
    public ResponseEntity<ApiResponse<String>> requestFriend(
            @SessionAttribute(name = LOGIN_MEMBER) LoginMember loginMember,
            @RequestBody String friendLoginId
    ) {
        FriendStatus friendStatus = memberService.requestFriend(loginMember.getId(), friendLoginId);

        ApiResponse<String> response = createResponseBy(friendStatus);

        return ResponseEntity.ok(response);
    }

    private ApiResponse<String> createResponseBy(FriendStatus friendStatus) {
        String message = "";
        switch (friendStatus) {
            case REQUESTED -> message = "친구 요청을 보냈습니다";
            case BLOCKED -> message = "친구 요청이 차단되어 있습니다";
            case ACCEPTED -> message = "이미 친구인 상태 입니다";
        }

        return new ApiResponse<>(HttpStatus.OK.value(), message);
    }

    private ResponseEntity<ErrorResponse> responsePasswordNotMatch() {
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST, "PASSWORD_NOT_MATCH", "비밀번호가 일치하지 않습니다")
                );
    }

    private boolean isNotSame(String password, String passwordCheck) {
        return !password.equals(passwordCheck);
    }

    private Cookie createPasswordResetCookie(String token) {
        Cookie cookie = new Cookie("password_reset_token", token);
        cookie.setPath("/");
        cookie.setMaxAge(300);
        cookie.setHttpOnly(true);
        return cookie;
    }

    private Cookie deletePasswordResetCookie() {
        Cookie cookie = new Cookie("password_reset_token", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
