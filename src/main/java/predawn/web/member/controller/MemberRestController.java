package predawn.web.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import predawn.domain.member.repository.MemberRepository;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberRestController {

    private final MemberRepository memberRepository;

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
}
