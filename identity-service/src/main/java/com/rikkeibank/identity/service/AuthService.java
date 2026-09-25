package com.rikkeibank.identity.service;

import com.rikkeibank.identity.domain.User;
import com.rikkeibank.identity.domain.UserRepository;
import com.rikkeibank.identity.security.JwtService;
import com.rikkeibank.identity.web.dto.AuthDtos.*;
import com.rikkeibank.identity.web.error.ApiException;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    public TokenResponse login(LoginRequest req) {
        User u = users.findByUsername(req.username())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sai tài khoản hoặc mật khẩu"));
        if (!u.isActive() || !encoder.matches(req.password(), u.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sai tài khoản hoặc mật khẩu");
        }
        return issue(u);
    }

    public TokenResponse refresh(RefreshRequest req) {
        Claims c = safeParse(req.refreshToken());
        if (!"refresh".equals(c.get("type"))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token không phải refresh");
        }
        User u = users.findById(Long.valueOf(c.getSubject()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User không tồn tại"));
        if (!u.isActive() || u.getTokenVersion() != (int) c.get("tv", Integer.class)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Phiên đã bị thu hồi");
        }
        return issue(u);
    }

    public IntrospectResponse introspect(String token) {
        try {
            Claims c = jwt.parse(token);
            if (!"access".equals(c.get("type"))) return new IntrospectResponse(false, null, null);
            User u = users.findById(Long.valueOf(c.getSubject())).orElse(null);
            if (u == null || !u.isActive()) return new IntrospectResponse(false, null, null);
            if (u.getTokenVersion() != (int) c.get("tv", Integer.class)) {
                return new IntrospectResponse(false, null, null); // đã bị thu hồi
            }
            return new IntrospectResponse(true, u.getId(), c.get("role", String.class));
        } catch (Exception e) {
            return new IntrospectResponse(false, null, null);
        }
    }

    @Transactional
    public void revoke(Long userId) {
        User u = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User không tồn tại"));
        u.setTokenVersion(u.getTokenVersion() + 1); // mọi access/refresh token cũ hết hiệu lực
        users.save(u);
    }

    private TokenResponse issue(User u) {
        String role = u.getRole().name();
        return new TokenResponse(
                jwt.generateAccess(u.getId(), role, u.getTokenVersion()),
                jwt.generateRefresh(u.getId(), role, u.getTokenVersion()),
                role);
    }

    private Claims safeParse(String token) {
        try { return jwt.parse(token); }
        catch (Exception e) { throw new ApiException(HttpStatus.UNAUTHORIZED, "Token không hợp lệ"); }
    }
}
