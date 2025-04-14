package com.heslin.postopia.enums.kafka;

import org.springframework.web.server.ServerWebInputException;

public enum VoteOperation {
    AGREED,
    DISAGREED,
    SWITCH_TO_AGREE,
    SWITCH_TO_DISAGREE
}
