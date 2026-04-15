package predawn.domain.file.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import predawn.domain.board.entity.Board;
import predawn.domain.common.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@SQLDelete(sql = "UPDATE board_attach_file SET deleted_at = CURRENT_TIMESTAMP WHERE board_attach_file_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class BoardAttachFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_attach_file_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_file_id")
    private UploadFile uploadFile;

    private LocalDateTime deletedAt;

    protected BoardAttachFile() {
    }

    public BoardAttachFile(Board board, UploadFile uploadFile) {
        this.board = board;
        this.uploadFile = uploadFile;
    }

    public void belongTo(Board board) {
        this.board = board;
    }
}
