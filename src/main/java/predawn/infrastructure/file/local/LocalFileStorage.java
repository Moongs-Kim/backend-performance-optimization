package predawn.infrastructure.file.local;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import predawn.application.file.enums.RootDirectory;
import predawn.domain.file.repository.FileStorage;
import predawn.domain.file.vo.DownloadFile;
import predawn.domain.file.vo.LocalDownloadFile;
import predawn.domain.file.vo.StoredFile;
import predawn.global.util.TimeFormatter;
import predawn.infrastructure.file.FileValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class LocalFileStorage implements FileStorage {

    @Value("${local.file.dir}")
    private String fileDir;

    @Value("${local.file.view}")
    private String fileViewPath;

    @Override
    public String getFullPath(String storageKey) {
        return fileDir + storageKey;
    }

    public String getViewPath(String storageKey) {
        return fileViewPath + "/" + storageKey;
    }

    @Override
    public List<StoredFile> stores(RootDirectory rootDirectory, List<MultipartFile> multipartFiles) {
        if (multipartFiles == null) return null;
        List<StoredFile> storedFiles = new ArrayList<>();

        uploads(rootDirectory, multipartFiles, storedFiles);

        return storedFiles;
    }

    @Override
    public StoredFile store(RootDirectory rootDirectory, MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) return null;
        if (rootDirectory == null) throw new RuntimeException("LocalFileStorage root is empty");

        String originalFilename = multipartFile.getOriginalFilename();
        String ext = extractExt(originalFilename);
        String contentType = multipartFile.getContentType();

        FileValidator.validate(ext, contentType);

        String storageKey = createStorageKey(rootDirectory.getRootDirectoryName(), ext);

        upload(multipartFile, storageKey);

        return new StoredFile(originalFilename, storageKey, multipartFile.getContentType(), multipartFile.getSize());
    }

    @Override
    public DownloadFile download(StoredFile storedFile) {
        String fullPath = getFullPath(storedFile.getStorageKey());
        return new LocalDownloadFile(fullPath, storedFile);
    }

    @Override
    public void deletes(List<String> storageKeys) {
        for (String storageKey : storageKeys) {
            delete(storageKey);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(Path.of(getFullPath(storageKey)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void uploads(RootDirectory rootDirectory, List<MultipartFile> multipartFiles, List<StoredFile> storedFiles) {
        try {
            for (MultipartFile multipartFile : multipartFiles) {
                if (!multipartFile.isEmpty()) {
                    storedFiles.add(store(rootDirectory, multipartFile));
                }
            }
        } catch (RuntimeException e) {
            storedFiles.forEach(storedFile -> delete(storedFile.getStorageKey()));
            throw e;
        }
    }

    private void upload(MultipartFile multipartFile, String storageKey) {
        String fullPath = getFullPath(storageKey);
        Path path = Paths.get(fullPath);
        try {
            Files.createDirectories(path.getParent());
            multipartFile.transferTo(new File(fullPath));
        } catch (IOException e) {
            delete(storageKey);
            throw new RuntimeException(e);
        }
    }

    private String createStorageKey(String root, String ext) {
        String yearMonth = TimeFormatter.fileNameFormat(LocalDate.now());
        String uuid = UUID.randomUUID().toString();
        return root + "/" + yearMonth + "/" + uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
}
