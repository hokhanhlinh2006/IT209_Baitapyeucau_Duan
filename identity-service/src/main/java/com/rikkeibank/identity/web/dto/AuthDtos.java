package com.rikkeibank.identity.web.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record TokenResponse(String accessToken, String refreshToken, String role) {}
    public record IntrospectRequest(@NotBlank String token) {}
    public record IntrospectResponse(boolean valid, Long uid, String role) {}
}
