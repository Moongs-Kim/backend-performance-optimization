package predawn.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import predawn.domain.common.BaseTimeEntity;
import predawn.domain.member.enums.FriendStatus;

@Entity
@Table(name = "friend",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_member_id_friend_member_id",
                columnNames = {"member_id", "friend_member_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Friend extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_member_id")
    private Member friendMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Member requestMember;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private FriendStatus friendStatus;

    private Friend(Member member, Member friendMember, Member requestMember, FriendStatus friendStatus) {
        this.member = member;
        this.friendMember = friendMember;
        this.requestMember = requestMember;
        this.friendStatus = friendStatus;
    }

    public static Friend create(Member member, Member friendMember, Member requestMember, FriendStatus friendStatus) {
        return new Friend(member, friendMember, requestMember, friendStatus);
    }

    public void changeFriendStatus(FriendStatus friendStatus) {
        this.friendStatus = friendStatus;
    }
}
