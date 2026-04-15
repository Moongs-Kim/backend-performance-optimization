package predawn.domain.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import predawn.domain.board.entity.BoardViewHistory;

public interface BoardViewHistoryRepository extends JpaRepository<BoardViewHistory, Long> {

    BoardViewHistory findByMemberIdAndBoardId(Long memberId, Long boardId);
}
