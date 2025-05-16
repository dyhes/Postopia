package com.heslin.postopia.common.kafka.enums;

//Danger!!!
//逻辑依赖于Ordinal,新增应于尾部
public enum SpaceOperation {
    MEMBER_JOINED,
    MEMBER_LEFT,
    POST_CREATED,
    POST_DELETED
}
