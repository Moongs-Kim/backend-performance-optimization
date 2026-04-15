package predawn.web.member.session;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class LoginMember implements Serializable {

    private Long id;
    private String name;
    private String filePath;

    public LoginMember(Long id, String name, String filePath) {
        this.id = id;
        this.name = name;
        this.filePath = filePath;
    }
}
