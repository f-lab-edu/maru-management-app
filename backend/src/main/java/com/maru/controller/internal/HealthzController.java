package com.maru.controller.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthzController {

    /**
     * health 체크
     *
     * @return 응답
     */
    @GetMapping("/internal/healthz")
    public String healthz() {
        return "OK";
    }
}
