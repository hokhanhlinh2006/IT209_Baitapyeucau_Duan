package com.rikkeibank.identity.web;

import com.rikkeibank.identity.service.AuthService;
import com.rikkeibank.identity.web.dto.AuthDtos.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @PostMapping("/auth/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return auth.refresh(req);
    }

    // Gateway gọi nội bộ để xác thực mỗi request
    @PostMapping("/internal/introspect")
    public IntrospectResponse introspect(@Valid @RequestBody IntrospectRequest req) {
        return auth.introspect(req.token());
    }

    // ADMIN ép một tài khoản đăng xuất ngay lập tức
    @PostMapping("/admin/users/{id}/revoke")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public void revoke(@PathVariable Long id) {
        auth.revoke(id);
    }
}
