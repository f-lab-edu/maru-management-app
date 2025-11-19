package com.maru.service.auth.dto;

public record GoogleUserInfoRes(
    String id,
    String email,
    String name,
    String picture
) {}
