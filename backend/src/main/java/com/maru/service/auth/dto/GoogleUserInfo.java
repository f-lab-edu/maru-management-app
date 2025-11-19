package com.maru.service.auth.dto;

public record GoogleUserInfo(
    String id,
    String email,
    String name,
    String picture
) {}
