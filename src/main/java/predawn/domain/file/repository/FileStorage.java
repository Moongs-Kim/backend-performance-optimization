package predawn.domain.file.repository;

import org.springframework.web.multipart.MultipartFile;
import predawn.application.file.enums.RootDirectory;
import predawn.domain.file.vo.DownloadFile;
import predawn.domain.file.vo.StoredFile;

import java.util.List;

public interface FileStorage {

    String getFullPath(String filename);

    List<StoredFile> stores(RootDirectory rootDirectory, List<MultipartFile> multipartFiles);

    StoredFile store(RootDirectory rootDirectory, MultipartFile multipartFile);

    DownloadFile download(StoredFile storedFile);

    void deletes(List<String> storageKeys);

    void delete(String storageKey);
}
