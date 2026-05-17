package com.thesis.sms_backend.auth.internal;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name="users", schema="auth")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    public enum Role {
        ADMIN, MANAGER, TEACHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", updatable = false, nullable = false, columnDefinition = "UUID")
    @Setter(AccessLevel.NONE)
    private UUID uuid;

    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name="password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private User.Role role;

    @Column(name = "active", nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Builder
    private User(UUID uuid, String login, String passwordHash, Role role, boolean active) {
        this.uuid = uuid;
        this.login = login;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }
}
