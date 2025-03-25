package com.heslin.postopia.dto.diff;

import com.heslin.postopia.enums.kafka.CommentOperation;
import com.heslin.postopia.enums.kafka.SpaceOperation;
import com.heslin.postopia.util.Utils;
import lombok.Getter;

public class SpaceDiff extends Diff {
    private long memberDiff;

    public SpaceDiff() {
        this.memberDiff = 0;
    }

    @Override
    public void updateDiff(int ordinal) {
        SpaceOperation spaceOperation = Utils.getEnumByOrdinal(SpaceOperation.class, ordinal);
        System.out.println("spaceOperation = " + spaceOperation);
        switch (spaceOperation) {
            case MEMBER_JOINED -> memberDiff++;
            case MEMBER_LEFT -> memberDiff--;
        }
    }


    @Override
    public String tableName() {
        return "spaces";
    }

    @Override
    public long getMemberDiff() {
        return memberDiff;
    }

    @Override
    public boolean shouldUpdateMember() {
        return memberDiff != 0;
    }
}
