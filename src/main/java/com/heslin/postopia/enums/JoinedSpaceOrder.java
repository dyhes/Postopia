package com.heslin.postopia.enums;

public enum JoinedSpaceOrder {
    JOINTIME("sui.createdAt"),
    LASTACTIVE("sui.lastActiveAt");

    private final String field;
    JoinedSpaceOrder(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
