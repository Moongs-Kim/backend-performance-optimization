package predawn.domain.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import predawn.domain.file.entity.BoardAttachFile;

import java.util.List;
import java.util.Optional;

public interface BoardAttachFileRepository extends JpaRepository<BoardAttachFile, Long> {

    @Query("SELECT ba FROM BoardAttachFile ba" +
           " JOIN FETCH ba.board b" +
           " JOIN FETCH ba.uploadFile uf" +
           " WHERE uf.id = :uploadFileId")
    Optional<BoardAttachFile> findByUploadFileId(@Param("uploadFileId") Long uploadFileId);

    @Query("SELECT bf FROM BoardAttachFile bf " +
           " WHERE bf.board.id = :boardId " +
           " AND bf.uploadFile.id = :fileId ")
    Optional<BoardAttachFile> findBoardAttachFile(@Param("boardId") Long boardId, @Param("fileId") Long fileId);

    @Query(value = "SELECT *" +
            " FROM board_attach_file bf" +
            //" WHERE bf.deleted_at <= DATE_SUB(NOW(), INTERVAL 10 MINUTE)" + //MySQL
            " WHERE bf.deleted_at <= DATEADD('MINUTE', -10, NOW())" + //H2
            " LIMIT :limit",
            nativeQuery = true)
    List<BoardAttachFile> findBoardFileCleanupTarget(@Param("limit") int limit);

    @Modifying
    @Query(value = "DELETE FROM board_attach_file" +
            " WHERE board_attach_file_id IN (:ids)", nativeQuery = true)
    void realDelete(@Param("ids") List<Long> ids);
}
