package predawn.domain.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import predawn.domain.like.entity.Like;

public interface LikeRepository extends JpaRepository<Like, Long> {

    @Query("SELECT COUNT(l) FROM Like l WHERE l.board.id = :boardId")
    Long countByBoardId(@Param("boardId") Long boardId);

    @Query("SELECT COUNT(l) = 1 FROM Like l WHERE l.member.id = :memberId AND l.board.id = :boardId")
    boolean isLikedByUser(@Param("memberId") Long memberId, @Param("boardId") Long boardId);

    Like findByMemberIdAndBoardId(Long memberId, Long boardId);
}
