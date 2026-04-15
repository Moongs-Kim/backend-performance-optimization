package predawn.domain.board.enums;

public enum BoardOpen {
    ALL("전체"), FRIENDS("친구");

    private final String description;

    BoardOpen(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
