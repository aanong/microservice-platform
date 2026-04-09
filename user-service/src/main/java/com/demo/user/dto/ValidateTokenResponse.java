package com.demo.user.dto;

import lombok.Data;

@Data
public class ValidateTokenResponse {

    private boolean valid;
    private Long userId;
}
