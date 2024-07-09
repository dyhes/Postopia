package com.heslin.postopia.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Credential {
    private String refreshToken;
    private String accessToken;
}
