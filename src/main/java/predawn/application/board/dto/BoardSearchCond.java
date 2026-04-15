package predawn.application.board.dto;

import lombok.Getter;
import predawn.application.board.enums.SearchType;
import predawn.application.board.enums.SortType;
import predawn.domain.common.exception.BadRequestException;

import static org.springframework.util.StringUtils.hasText;

@Getter
public class BoardSearchCond {
    private SearchType searchType;
    private String keyword;
    private SortType sortType = SortType.LATEST;;

    private BoardSearchCond(String searchType, String keyword, String sortType) {
        try {
            if (hasText(searchType)) {
                this.searchType = SearchType.valueOf(searchType.toUpperCase());
            }

            this.keyword = (hasText(keyword)) ? keyword : "";

            if (hasText(sortType)) {
                this.sortType = SortType.valueOf(sortType.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e);
        }
    }

    public static BoardSearchCond of(String searchType, String keyword, String sortType) {
        return new BoardSearchCond(searchType, keyword, sortType);
    }

}
