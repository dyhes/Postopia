package com.heslin.postopia.dto;

import java.time.Instant;

public record PostDraftDto(
        Long id,
        String subject,
        String content,
        Long spaceId,
        String spaceName,
        String spaceAvatar,
        Instant updatedAt
) {}
