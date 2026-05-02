package predawn.domain.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import predawn.domain.common.BaseTimeEntity;
import predawn.domain.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Getter
public class BoardViewHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_view_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    private LocalDateTime viewAt;

    protected BoardViewHistory() {
    }

    public BoardViewHistory(Member member, Board board, LocalDateTime viewAt) {
        this.member = member;
        this.board = board;
        this.viewAt = viewAt;
    }

    public void changeViewAt(LocalDateTime recentViewTime) {
        viewAt = recentViewTime;
    }
}
