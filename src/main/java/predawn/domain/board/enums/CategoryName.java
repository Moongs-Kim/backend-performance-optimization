package predawn.domain.board.enums;

public enum CategoryName {
    GENERAL("자유게시판"), NOTICE("공지사항"), QUESTION("질문");

    private final String description;

    CategoryName(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
