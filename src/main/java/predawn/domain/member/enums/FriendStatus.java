package predawn.domain.member.enums;

public enum FriendStatus {
    REQUESTED("요청중"), ACCEPTED("친구"), REJECTED("거절"),
    CANCELED("요청 취소"), BLOCKED("차단");

    private final String description;

    FriendStatus(String description) {
        this.description = description;
    }
}
