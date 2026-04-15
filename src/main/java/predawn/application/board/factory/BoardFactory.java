package predawn.application.board.factory;

import predawn.application.board.dto.BoardPostCommand;
import predawn.domain.board.entity.Board;
import predawn.domain.board.entity.Category;
import predawn.domain.member.entity.Member;

public abstract class BoardFactory {

    public static Board create(Member member, BoardPostCommand postCommand, Category category) {
        return new Board(
                member,
                postCommand.getTitle(),
                postCommand.getContent(),
                postCommand.getBoardOpen(),
                category
        );
    }
}
