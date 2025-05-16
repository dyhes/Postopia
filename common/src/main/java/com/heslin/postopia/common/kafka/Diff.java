package com.heslin.postopia.common.kafka;

public abstract class Diff {
    abstract public void updateDiff(int ordinal);

    public boolean shouldUpdatePositive() {
        return false;
    }

    public boolean shouldUpdateNegative() {
        return false;
    }

    public boolean shouldUpdatePost() {
        return false;
    }

    public boolean shouldUpdateComment() {
        return false;
    }

    public boolean shouldUpdateMember() {
        return false;
    }

    public boolean shouldUpdateCredit() {
        return false;
    }

    public long getPositiveDiff() {
        return 0;
    }

    public long getNegativeDiff() {
        return 0;
    }

    public long getCommentDiff() {
        return 0;
    }

    public long getMemberDiff() {
        return 0;
    }

    public long getPostDiff() {
        return 0;
    }

    public long getCreditDiff() {
        return 0;
    }
}
