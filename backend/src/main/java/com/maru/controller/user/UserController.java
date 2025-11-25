package com.maru.controller.user;

import com.maru.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    @PostMapping("/onboarding/profile")
    public ResponseEntity<?> updateOnboardingProfile(@RequestBody Map<String, String> request) {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    @PostMapping("/onboarding/role")
    public ResponseEntity<?> updateOnboardingRole(@RequestBody Map<String, String> request) {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    @PostMapping("/onboarding/dojang")
    public ResponseEntity<?> createDojang(@RequestBody Map<String, String> request) {
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }
}