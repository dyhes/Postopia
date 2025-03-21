package com.heslin.postopia.dto.user;

import java.io.Serializable;

public class UserId implements Serializable {
    private static final long MASK = 0x5A5A5A5A5A5A5A5AL;
    private final Long id;
    public UserId(Long content) {
        this.id = content;
    }

//    public UserId(String code) {
//        this.id = masked(Long.parseLong(code));
//    }

    public Long getId() {
        return id;
    }

    public Long getMaskedId() {
        return masked(id);
    }

    public static Long masked(Long id) {
        return id ^ MASK;
    }

    @Override
    public String toString() {
        return getMaskedId().toString();
    }


}
