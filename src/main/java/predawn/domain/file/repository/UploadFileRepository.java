package predawn.domain.file.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import predawn.domain.file.entity.UploadFile;

import java.util.List;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {

    @Query("SELECT uf FROM UploadFile uf" +
           " LEFT JOIN uf.boardAttachFiles bf" +
           " LEFT JOIN FETCH uf.member m" +
           " WHERE bf.id IS NULL" +
           " AND m.profileImage.id IS NULL")
    List<UploadFile> findMemberFileCleanupTarget(Pageable pageable);
}
