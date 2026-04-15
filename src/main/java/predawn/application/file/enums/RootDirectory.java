package predawn.application.file.enums;

public enum RootDirectory {
    MEMBER("member"), BOARD("board");

    private final String rootDirectoryName;

    RootDirectory(String rootDirectoryName) {
        this.rootDirectoryName = rootDirectoryName;
    }

    public String getRootDirectoryName() {
        return rootDirectoryName;
    }
}
