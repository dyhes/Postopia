package com.heslin.postopia.user.dto;

public record Credential(Long userId, String refreshToken, String accessToken) {}