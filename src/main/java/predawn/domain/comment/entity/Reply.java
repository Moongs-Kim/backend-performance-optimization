package predawn.domain.comment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import predawn.domain.common.BaseTimeEntity;
import predawn.domain.member.entity.Member;

@Entity
@Getter
public class Reply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "reply_content")
    private String content;

    protected Reply() {
    }

    public Reply(Member member, Comment comment, String content) {
        this.member = member;
        this.comment = comment;
        this.content = content;
    }

    public void belongTo(Comment comment) {
        this.comment = comment;
    }
}
