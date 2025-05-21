package com.heslin.postopia.space.dto;

import java.time.Instant;

public record SearchSpaceInfo(Long id, String avatar, Long memberCount, Long postCount, Instant createdAt) {}
