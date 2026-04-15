package predawn.web.board.dto;

import lombok.Getter;

@Getter
public class BoardsReqDto {
    private String searchType;
    private String keyword;
    private String sortType;
    private Integer pageIndex;

    public BoardsReqDto(String searchType, String keyword, String sortType, Integer pageIndex) {
        this.searchType = searchType;
        this.keyword = keyword;
        this.sortType = sortType;
        this.pageIndex = pageIndex;
    }
}
