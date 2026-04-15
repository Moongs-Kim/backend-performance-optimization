package predawn.application.like.dto;

import lombok.Getter;

@Getter
public class LikeToggleResult {

    private long likeCount;
    private boolean isLiked;

    public LikeToggleResult(long likeCount, boolean isLiked) {
        this.likeCount = likeCount;
        this.isLiked = isLiked;
    }
}
