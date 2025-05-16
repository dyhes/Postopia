package com.heslin.postopia.common.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.heslin.postopia.common.serializer.UserIdSerializer;

import java.io.Serializable;

@JsonSerialize(using = UserIdSerializer.class)
public class UserId implements Serializable {
    private static final long MASK = 0x5A5A5A5A5A5A5A5AL;
    private final Long id;
    public UserId(Long content) {
        this.id = content;
    }

    public Long getId() {
        return id;
    }

    public Long getMaskedId() {
        return masked(id);
    }

    public static Long masked(Long id) {
        if (id != null) {
            return id ^ MASK;
        }
        return -1L;
    }

    public static String encode(Long id) {
        return masked(id).toString();
    }

    @Override
    public String toString() {
        return getMaskedId().toString();
    }
}
