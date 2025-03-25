package com.heslin.postopia.dto.diff;

public abstract class Diff {
    public boolean shouldUpdatePositive() {
        return false;
    }

    public boolean shouldUpdateNegative() {
        return false;
    }

    public boolean shouldUpdateComment() {
        return false;
    }

    public boolean shouldUpdateMember() {
        return false;
    }

    abstract public void updateDiff(int ordinal);

    abstract public String tableName();

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
}
