package com.heslin.postopia.comment.kafka;

import com.heslin.postopia.common.kafka.Diff;
import com.heslin.postopia.common.kafka.enums.CommentOperation;
import com.heslin.postopia.common.utils.Utils;

public class CommentDiff extends Diff {
    private long positiveDiff;
    private long negativeDiff;

    public CommentDiff() {
        this.negativeDiff = 0;
        this.positiveDiff = 0;
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
    public boolean shouldUpdatePositive() {
        return positiveDiff != 0;
    }

    @Override
    public boolean shouldUpdateNegative() {
        return negativeDiff != 0;
    }

    @Override
    public void updateDiff(int ordinal) {
        CommentOperation commentOperation = Utils.getEnumByOrdinal(CommentOperation.class, ordinal);
        System.out.println("commentOperation = " + commentOperation);
        switch (commentOperation) {
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
            case CANCEL_DISLIKE -> negativeDiff--;
            case CANCEL_LIKE -> positiveDiff--;
        }
    }
}
