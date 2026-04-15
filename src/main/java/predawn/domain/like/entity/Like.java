package predawn.domain.like.entity;

import jakarta.persistence.*;
import lombok.Getter;
import predawn.domain.board.entity.Board;
import predawn.domain.common.BaseTimeEntity;
import predawn.domain.member.entity.Member;

@Entity
@Table(name = "likes")
@Getter
public class Like extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    protected Like() {
    }

    public Like(Member member, Board board) {
        this.member = member;
        this.board = board;
    }

    public void belongTo(Board board) {
        this.board = board;
    }
}
