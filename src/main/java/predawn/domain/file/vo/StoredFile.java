package predawn.domain.file.vo;

import lombok.Getter;

@Getter
public class StoredFile {
    private final String uploadFileName;
    private final String storageKey;
    private final String contentType;
    private final Long size;

    public StoredFile(String uploadFileName, String storageKey, String contentType, Long size) {
        this.uploadFileName = uploadFileName;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.size = size;
    }
}
