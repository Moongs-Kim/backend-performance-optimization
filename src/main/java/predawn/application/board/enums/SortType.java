package predawn.application.board.enums;

public enum SortType {
    LATEST_TOP_N_LIKE_COUNT, LATEST, VIEW_COUNT;

    public boolean isTopNSort() {
        return this == LATEST_TOP_N_LIKE_COUNT;
    }
}
