package predawn.domain.file.vo;

import predawn.domain.file.enums.DownloadType;

public class LocalDownloadFile extends DownloadFile {

    public LocalDownloadFile(String path, StoredFile storedFile) {
        super(DownloadType.LOCAL, path, storedFile);
    }
}
