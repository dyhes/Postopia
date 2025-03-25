package com.heslin.postopia.dto.diff;

import com.heslin.postopia.enums.kafka.PostOperation;
import com.heslin.postopia.util.Utils;
import lombok.Getter;


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
        }
    }

    @Override
    public String tableName() {
        return "posts";
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
