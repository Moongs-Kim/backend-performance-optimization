package predawn.application.file.dto;

import lombok.Getter;
import predawn.domain.file.entity.UploadFile;

@Getter
public class UploadFileResult {
    private Long uploadFileId;
    private String uploadFileName;

    public UploadFileResult(UploadFile uploadFile) {
        this.uploadFileId = uploadFile.getId();
        this.uploadFileName = uploadFile.getUploadFileName();
    }
}
