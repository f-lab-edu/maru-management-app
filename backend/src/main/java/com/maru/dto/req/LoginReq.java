package com.maru.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 *
 * TODO: 소셜 로그인 구현 시 교체 예정
 */
@Getter
@NoArgsConstructor
public class LoginReq {

    @NotBlank(message = "사용자명을 입력해주세요")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}
