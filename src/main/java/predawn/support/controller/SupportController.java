package predawn.support.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import predawn.domain.member.entity.Member;
import predawn.domain.member.repository.MemberRepository;
import predawn.web.member.session.LoginMember;

import static predawn.web.member.session.SessionConst.LOGIN_MEMBER;

@Controller
@Profile({"dev", "load"})
@RequiredArgsConstructor
public class SupportController {

    private final MemberRepository memberRepository;

    @GetMapping("/dev/login-test")
    public String mockLogin(HttpSession session) {
        Member member = memberRepository.findById(1L).orElseThrow();

        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), null);

        session.setAttribute(LOGIN_MEMBER, loginMember);

        return "redirect:/boards";
    }
}
