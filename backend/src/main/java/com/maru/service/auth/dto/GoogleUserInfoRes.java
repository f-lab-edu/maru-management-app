package com.maru.service.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record GoogleUserInfoRes(
    @JsonAlias({"sub", "id"})
    String id,
    String email,
    String name,
    String picture
) {}
