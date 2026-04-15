package predawn.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import predawn.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m WHERE m.loginId = :loginId AND m.password = :password")
    Optional<Member> login(@Param("loginId") String loginId, @Param("password") String password);

    Member findMemberByLoginId(String loginId);
}
