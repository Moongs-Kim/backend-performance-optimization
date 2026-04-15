package predawn.global.pagination;

import lombok.Getter;

@Getter
public class PageInformation {
    private static final int DEFAULT_INDEX = 0;
    private static final int DEFAULT_SIZE = 10;

    private int pageIndex;
    private int pageSize;

    public PageInformation() {
        this(DEFAULT_INDEX, DEFAULT_SIZE);
    }

    public PageInformation(int pageIndex) {
        this(pageIndex, DEFAULT_SIZE);
    }

    public PageInformation(int pageIndex, int pageSize) {
        validatePageInfo(pageIndex, pageSize);
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    private void validatePageInfo(int pageIndex, int pageSize) {
        if (pageIndex < 0 || pageSize <= 0) throw new PageBadRequestException();
    }

}
