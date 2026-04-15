package predawn.domain.comment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import predawn.domain.board.entity.Board;
import predawn.domain.common.BaseTimeEntity;
import predawn.domain.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Column(name = "comment_content")
    private String content;

    @OneToMany(mappedBy = "comment")
    private List<Reply> replies = new ArrayList<>();

    protected Comment() {
    }

    public Comment(Member member, Board board, String content) {
        this.member = member;
        this.board = board;
        this.content = content;
    }

    public void addReply(Reply reply) {
        replies.add(reply);
        reply.belongTo(this);
    }
}
