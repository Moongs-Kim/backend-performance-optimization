package predawn.domain.file.vo;

import predawn.domain.file.enums.DownloadType;

public class ExternalDownloadFile extends DownloadFile {

    public ExternalDownloadFile(String path, StoredFile storedFile) {
        super(DownloadType.EXTERNAL, path, storedFile);
    }
}
