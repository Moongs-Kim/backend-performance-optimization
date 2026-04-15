package predawn.global.pagination;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class Paging {
    private static final int PAGE_PER_BLOCK = 3;

    private int nowPage;
    private int nowBlock;

    private int totalPage;
    private int totalBlock;

    private int beginPage;
    private int endPage;

    private boolean hasNextPage;
    private boolean hasPreviousPage;

    private boolean isFirstBlock;
    private boolean isLastBlock;

    public Paging(Page<?> page) {
        nowPage = page.getNumber();
        totalPage = page.getTotalPages();
        initTotalBlock();
        nowBlock = nowPage / PAGE_PER_BLOCK;
        beginPage = nowBlock * PAGE_PER_BLOCK;
        initEndPage();
        hasNextPage = page.hasNext();
        hasPreviousPage = page.hasPrevious();
        initFirstBlock();
        initLastBlock();
    }

    private void initTotalBlock() {
        totalBlock = totalPage / PAGE_PER_BLOCK - 1;
        if (totalPage % PAGE_PER_BLOCK > 0) totalBlock++;
    }

    private void initEndPage() {
        endPage = (nowBlock != totalBlock) ? beginPage + PAGE_PER_BLOCK - 1 : totalPage - 1;
    }

    private void initFirstBlock() {
        isFirstBlock = nowBlock == 0;
    }

    private void initLastBlock() {
        isLastBlock = nowBlock == totalBlock;
    }
}
