package com.heslin.postopia.dto;

import java.time.Instant;

public record SpaceInfo(Long id, String name, String avatar, String description, Instant createdAt, Long memberCount){}
