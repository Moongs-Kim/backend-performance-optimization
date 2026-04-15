package predawn.domain.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import predawn.domain.board.enums.BoardOpen;
import predawn.domain.common.BaseTimeEntity;
import predawn.domain.file.entity.BoardAttachFile;
import predawn.domain.like.entity.Like;
import predawn.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SQLDelete(sql = "UPDATE board SET deleted_at = CURRENT_TIMESTAMP WHERE board_id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private BoardOpen boardOpen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private int viewCount;

    @OneToMany(mappedBy = "board")
    private List<BoardAttachFile> attachFiles = new ArrayList<>();

    @OneToMany(mappedBy = "board")
    private List<Like> likes = new ArrayList<>();

    private LocalDateTime deletedAt;

    @Version
    private Long version;

    protected Board() {
    }

    public Board(Member member, String title, String content, BoardOpen boardOpen, Category category) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.boardOpen = boardOpen;
        this.category = category;
    }

    public void changeContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void changeBoardOpen(BoardOpen boardOpen) {
        this.boardOpen = boardOpen;
    }

    public void changeCategory(Category category) {
        this.category = category;
    }

    public void addAttachFile(BoardAttachFile attachFile) {
        attachFiles.add(attachFile);
        attachFile.belongTo(this);
    }

    public void addLike(Like like) {
        likes.add(like);
        like.belongTo(this);
    }

    public List<Like> getLikes() {
        return List.copyOf(likes);
    }
}
