package predawn.application.board.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import predawn.domain.common.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BoardSearchCondTest {

    @DisplayName("잘못된 검색 문자를 사용하면 예외가 발생한다")
    @Test
    public void wrongSearchType() {
        String searchType = "wrong_title";
        String keyword = "";
        String sortType = "latest";

        assertThrows(BadRequestException.class, () -> BoardSearchCond.of(searchType, keyword, sortType));
    }

    @DisplayName("잘못된 정렬 문자를 사용하면 예외가 발생한다")
    @Test
    public void wrongSortType() {
        String searchType = "title";
        String keyword = "";
        String sortType = "wrong_latest";

        assertThrows(BadRequestException.class, () -> BoardSearchCond.of(searchType, keyword, sortType));
    }

}