package predawn.application.file.mapper;

import predawn.domain.file.entity.UploadFile;
import predawn.domain.file.vo.StoredFile;

public abstract class FileMapper {

    public static UploadFile toEntity(StoredFile storedFile) {
        return new UploadFile(
                storedFile.getUploadFileName(),
                storedFile.getStorageKey(),
                storedFile.getContentType(),
                storedFile.getSize()
        );
    }

    public static StoredFile toStoredFile(UploadFile uploadFile) {
        return new StoredFile(
                uploadFile.getUploadFileName(),
                uploadFile.getStorageKey(),
                uploadFile.getContentType(),
                uploadFile.getSize()
        );
    }
}
