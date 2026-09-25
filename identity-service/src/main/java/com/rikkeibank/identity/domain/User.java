package com.rikkeibank.identity.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;      // BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    // Tăng mỗi lần ADMIN thu hồi phiên -> token cũ (tv nhỏ hơn) hết hiệu lực ngay
    @Column(nullable = false)
    private int tokenVersion = 0;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
    public Role getRole() { return role; }
    public void setRole(Role v) { this.role = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public int getTokenVersion() { return tokenVersion; }
    public void setTokenVersion(int v) { this.tokenVersion = v; }
}
