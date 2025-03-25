package com.heslin.postopia.dto.post;

import com.heslin.postopia.enums.kafka.PostOperation;
import com.heslin.postopia.util.Utils;
import lombok.Data;
import lombok.Getter;

@Getter
public class PostDiff {
    private long positiveDiff;
    private long negativeDiff;
    private long commentDiff;

    public PostDiff() {
        this.positiveDiff = 0;
        this.negativeDiff = 0;
        this.commentDiff = 0;
    }

    public boolean shouldUpdatePositive() {
        return positiveDiff != 0;
    }

    public boolean shouldUpdateNegative() {
        return negativeDiff != 0;
    }

    public boolean shouldUpdateComment() {
        return commentDiff != 0;
    }

    public void upDateDiff(int ordinal) {
        PostOperation postOperation = Utils.getEnumByOrdinal(PostOperation.class, ordinal);
        switch (postOperation) {
            case LIKED -> positiveDiff++;
            case DISLIKED -> negativeDiff++;
            case SWITCH_TO_LIKE -> {
                positiveDiff++;
                negativeDiff--;
            }
            case SWITCH_TO_DISLIKE -> {
                positiveDiff--;
                negativeDiff++;
            }
            case COMMENT_CREATED -> commentDiff++;
            case COMMENT_DELETED -> commentDiff--;
        }
    }
}
