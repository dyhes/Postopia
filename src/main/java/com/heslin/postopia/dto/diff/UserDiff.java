package com.heslin.postopia.dto.diff;

import com.heslin.postopia.enums.kafka.SpaceOperation;
import com.heslin.postopia.enums.kafka.UserOperation;
import com.heslin.postopia.util.Utils;

public class UserDiff extends Diff {
    private long postDiff;
    private long commentDiff;
    private long creditDiff;

    public UserDiff() {
        this.postDiff = 0L;
        this.commentDiff = 0L;
        this.creditDiff = 0L;
    }

    @Override
    public long getCreditDiff() {
        return creditDiff;
    }

    @Override
    public long getCommentDiff() {
        return commentDiff;
    }

    @Override
    public long getPostDiff() {
        return postDiff;
    }

    @Override
    public boolean shouldUpdatePost() {
        return postDiff != 0;
    }

    @Override
    public boolean shouldUpdateComment() {
        return commentDiff != 0;
    }

    @Override
    public boolean shouldUpdateCredit() {
        return creditDiff != 0;
    }

    @Override
    public void updateDiff(int ordinal) {
        UserOperation userOperation = Utils.getEnumByOrdinal(UserOperation.class, ordinal);
        switch (userOperation) {
            case COMMENT_CREATED -> commentDiff++;
            case COMMENT_DELETED -> commentDiff--;
            case POST_CREATED -> postDiff++;
            case POST_DELETED -> postDiff--;
            case CREDIT_EARNED -> creditDiff++;
        }
    }
}
