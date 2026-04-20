package predawn.global.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PageInformationTest {

    @DisplayName("페이지 인덱스가 음수이면 예외가 발생한다")
    @Test
    void wrongPageIndex() {
        int wrongPageIndex = -1;

        assertThrows(PageBadRequestException.class, () -> new PageInformation(wrongPageIndex));
    }

    @DisplayName("페이지 사이즈가 0이하이면 예외가 발생한다")
    @Test
    void wrongPageSize() {
        int pageIndex = 0;
        int wrongPageSize = 0;

        assertThrows(PageBadRequestException.class, () -> new PageInformation(pageIndex, wrongPageSize));
    }

}