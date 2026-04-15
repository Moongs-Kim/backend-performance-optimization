package predawn.domain.file.entity;

import jakarta.persistence.*;
import lombok.Getter;
import predawn.domain.common.BaseTimeEntity;
import predawn.domain.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class UploadFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upload_file_id")
    private Long id;

    @OneToOne(mappedBy = "profileImage")
    private Member member;

    @OneToMany(mappedBy = "uploadFile")
    private List<BoardAttachFile> boardAttachFiles = new ArrayList<>();

    private String uploadFileName;
    private String storageKey;

    private String contentType;
    private Long size;

    protected UploadFile() {
    }

    public UploadFile(String uploadFileName, String storageKey, String contentType, Long size) {
        this.uploadFileName = uploadFileName;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.size = size;
    }
}
