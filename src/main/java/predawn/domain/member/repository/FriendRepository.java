package predawn.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import predawn.domain.member.entity.Friend;
import predawn.domain.member.entity.Member;

import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    Optional<Friend> findByMemberAndFriendMember(Member member, Member friendMember);

    boolean existsByMemberAndFriendMember(Member member, Member friendMember);
}
