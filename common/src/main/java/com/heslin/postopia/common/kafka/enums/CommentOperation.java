package com.heslin.postopia.common.kafka.enums;


//Danger!!!
//逻辑依赖于Ordinal,新增应于尾部
public enum CommentOperation {
    LIKED,
    DISLIKED,
    SWITCH_TO_LIKE,
    SWITCH_TO_DISLIKE,
    CANCEL_LIKE,
    CANCEL_DISLIKE
}
