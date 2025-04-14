package com.heslin.postopia.dto.diff;

import com.heslin.postopia.enums.kafka.VoteOperation;
import com.heslin.postopia.util.Utils;

public class VoteDiff extends Diff{
    private long positiveDiff;
    private long negativeDiff;
    @Override
    public void updateDiff(int ordinal) {
        VoteOperation voteOperation = Utils.getEnumByOrdinal(VoteOperation.class, ordinal);
        switch (voteOperation) {
            case AGREED -> positiveDiff++;
            case DISAGREED -> negativeDiff++;
            case SWITCH_TO_AGREE -> {
                positiveDiff++;
                negativeDiff--;
            }
            case SWITCH_TO_DISAGREE -> {
                positiveDiff--;
                negativeDiff++;
            }
        }
    }

    public VoteDiff() {
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
    public String tableName() {
        return "votes";
    }
}
