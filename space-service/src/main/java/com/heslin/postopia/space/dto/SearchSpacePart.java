package com.heslin.postopia.space.dto;

import java.time.Instant;

public record SearchSpacePart(Long id, String avatar, Long memberCount, Long postCount, Instant createdAt) {}
