package predawn.domain.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import predawn.domain.board.entity.BoardViewHistory;

import java.util.Optional;

public interface BoardViewHistoryRepository extends JpaRepository<BoardViewHistory, Long> {

    @Query("SELECT v FROM BoardViewHistory v WHERE v.board.id = :boardId AND v.member.id = :memberId")
    Optional<BoardViewHistory> findByBoardIdAndMemberId(@Param("boardId") Long boardId, @Param("memberId") Long memberId);
}
