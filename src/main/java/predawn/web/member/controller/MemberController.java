package predawn.web.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import predawn.application.member.dto.MemberSignupCommand;
import predawn.application.member.service.MemberService;
import predawn.domain.member.enums.Gender;
import predawn.web.member.dto.LoginForm;
import predawn.web.member.dto.MemberSignupForm;
import predawn.web.member.mapper.MemberCommandMapper;
import predawn.web.member.session.LoginMember;
import predawn.web.member.validator.SignupValidator;

import static predawn.web.member.session.SessionConst.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String LoginPage(@RequestParam(defaultValue = "/") String redirectURL, Model model) {
        model.addAttribute("redirectURL", redirectURL);
        model.addAttribute("loginForm", new LoginForm());
        return "member/login";
    }

    @PostMapping("/login")
    public String login(
            @Validated @ModelAttribute("loginForm") LoginForm loginForm, BindingResult bindingResult,
            @RequestParam(defaultValue = "/") String redirectURL,
            HttpServletRequest request)
    {
        if (bindingResult.hasErrors()) return "member/login";

        LoginMember loginMember = memberService.login(loginForm.getLoginId(), loginForm.getPassword());

        if (loginMember == null) {
            bindingResult.reject("", "아이디 또는 비밀번호가 맞지 않습니다");
            return "member/login";
        }

        if (!redirectURL.startsWith("/")) {
            redirectURL = "/";
        }

        HttpSession session = request.getSession();
        session.setAttribute(LOGIN_MEMBER, loginMember);

        return "redirect:" + redirectURL;
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupForm", new MemberSignupForm());
        return "member/signup";
    }

    @PostMapping("/signup")
    public String signup(
            @Valid @ModelAttribute("signupForm") MemberSignupForm signupForm,
            BindingResult bindingResult)
    {
        SignupValidator signupValidator = new SignupValidator(signupForm, bindingResult);

        signupValidator.validate();

        if (bindingResult.hasErrors()) return "member/signup";

        MemberSignupCommand memberSignupCommand = MemberCommandMapper.toSignupCommand(signupForm);

        memberService.signup(memberSignupCommand, signupForm.getProfileImage());
        return "redirect:/login";
    }

    @GetMapping("/member/find-id")
    public String findMemberLoginIdPage() {
        return "member/find-login-id";
    }

    @GetMapping("/member/password-reset")
    public String passwordResetPage() {
        return "member/password-reset";
    }

    @ModelAttribute("genders")
    private Gender[] genders() {
        return Gender.values();
    }

}
