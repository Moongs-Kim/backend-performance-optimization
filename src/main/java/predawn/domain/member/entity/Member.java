package predawn.domain.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import predawn.domain.common.BaseTimeEntity;
import predawn.domain.file.entity.UploadFile;
import predawn.domain.member.enums.Gender;

import java.time.LocalDate;

@Entity
@Getter
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String loginId;
    private String password;
    private String name;
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String email;
    private String address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_file_id")
    private UploadFile profileImage;

    protected Member() {
    }

    public Member(String loginId, String password, String name, LocalDate birthDate,
                  Gender gender, String email, String address, UploadFile profileImage) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.email = email;
        this.address = address;
        this.profileImage = profileImage;
    }
}
