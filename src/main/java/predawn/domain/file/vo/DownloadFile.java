package predawn.domain.file.vo;

import lombok.Getter;
import predawn.domain.file.enums.DownloadType;

@Getter
public abstract class DownloadFile {
    private final DownloadType type;
    private final String path;
    private final String filename;
    private final String contentType;
    private final Long size;

    protected DownloadFile(DownloadType type, String path, StoredFile storedFile) {
        this.type = type;
        this.path = path;
        this.filename = storedFile.getUploadFileName();
        this.contentType = storedFile.getContentType();
        this.size = storedFile.getSize();
    }
}
