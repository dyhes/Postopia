package com.heslin.postopia.space.dto;

import java.time.Instant;

public record SpaceInfo(Long id, String name, String avatar, String description, Instant createdAt, Long postCount, Long memberCount){}