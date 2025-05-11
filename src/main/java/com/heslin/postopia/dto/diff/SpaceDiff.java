package com.heslin.postopia.dto.diff;
import com.heslin.postopia.enums.kafka.SpaceOperation;
import com.heslin.postopia.util.Utils;

public class SpaceDiff extends Diff {
    private long memberDiff;
    private long postDiff;

    public SpaceDiff() {
        this.memberDiff = 0;
        this.postDiff = 0;
    }

    @Override
    public void updateDiff(int ordinal) {
        SpaceOperation spaceOperation = Utils.getEnumByOrdinal(SpaceOperation.class, ordinal);
        System.out.println("spaceOperation = " + spaceOperation);
        switch (spaceOperation) {
            case MEMBER_JOINED -> memberDiff++;
            case MEMBER_LEFT -> memberDiff--;
            case POST_CREATED -> postDiff++;
            case POST_DELETED -> postDiff--;
        }
    }

    @Override
    public long getMemberDiff() {
        return memberDiff;
    }

    @Override
    public boolean shouldUpdatePost() {
        return postDiff != 0;
    }

    @Override
    public boolean shouldUpdateMember() {
        return memberDiff != 0;
    }
}
