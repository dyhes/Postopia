package com.heslin.postopia.post.kafka;

import com.heslin.postopia.common.kafka.Diff;
import com.heslin.postopia.common.kafka.enums.PostOperation;
import com.heslin.postopia.common.utils.Utils;


public class PostDiff extends Diff {
    private long positiveDiff;
    private long negativeDiff;
    private long commentDiff;


    public PostDiff() {
        this.positiveDiff = 0;
        this.negativeDiff = 0;
        this.commentDiff = 0;
    }

    @Override
    public boolean shouldUpdatePositive() {
        return positiveDiff != 0;
    }

    @Override
    public boolean shouldUpdateNegative() {
        return negativeDiff != 0;
    }

    @Override
    public boolean shouldUpdateComment() {
        return commentDiff != 0;
    }

    @Override
    public void updateDiff(int ordinal) {
        PostOperation postOperation = Utils.getEnumByOrdinal(PostOperation.class, ordinal);
        System.out.println("postOperation = " + postOperation);
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
            case CANCEL_DISLIKE -> negativeDiff--;
            case CANCEL_LIKE -> positiveDiff--;
        }
    }

    @Override
    public long getPositiveDiff() {
        return positiveDiff;
    }

    @Override
    public long getNegativeDiff() {
        return negativeDiff;
    }

    @Override
    public long getCommentDiff() {
        return commentDiff;
    }
}
