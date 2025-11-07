package com.maru.controller.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인 응답 DTO
 *
 */
@Getter
@AllArgsConstructor
public class LoginRes {

    private Long userId;
    private String role;
    private String message;
}
