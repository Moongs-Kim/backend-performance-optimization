package predawn.application.file.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import predawn.domain.file.entity.BoardAttachFile;
import predawn.domain.file.entity.UploadFile;
import predawn.domain.file.repository.BoardAttachFileRepository;
import predawn.domain.file.repository.FileStorage;
import predawn.domain.file.repository.UploadFileRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileCleanupService {

    private final FileStorage fileStorage;
    private final BoardAttachFileRepository boardAttachFileRepository;
    private final UploadFileRepository uploadFileRepository;

    @Transactional
    public void boardFileCleanup() {
        while (true) {
            List<BoardAttachFile> cleanupTarget = boardAttachFileRepository.findBoardFileCleanupTarget(100);

            if (cleanupTarget.isEmpty()) {
                log.info("cleanupTarget empty, boardFileCleanup stop");
                break;
            }

            List<Long> boardAttachFileIds = new ArrayList<>();
            List<UploadFile> uploadFiles = new ArrayList<>();
            List<String> storageKeys = new ArrayList<>();

            for (BoardAttachFile bf : cleanupTarget) {
                UploadFile uploadFile = bf.getUploadFile();
                boardAttachFileIds.add(bf.getId());
                uploadFiles.add(uploadFile);
                storageKeys.add(uploadFile.getStorageKey());
            }

            boardAttachFileRepository.realDelete(boardAttachFileIds);

            uploadFiles.forEach(uploadFileRepository::delete);

            fileStorage.deletes(storageKeys);
        }
    }

    public void memberFileCleanup() {
        while (true) {
            List<UploadFile> cleanupTarget = uploadFileRepository.findMemberFileCleanupTarget(PageRequest.ofSize(100));

            if (cleanupTarget.isEmpty()) {
                log.info("cleanupTarget empty, memberFileCleanup stop");
                break;
            }

            List<String> storageKeys = new ArrayList<>();
            for (UploadFile uploadFile : cleanupTarget) {
                uploadFileRepository.delete(uploadFile);
                storageKeys.add(uploadFile.getStorageKey());
            }

            fileStorage.deletes(storageKeys);
        }
    }
}
