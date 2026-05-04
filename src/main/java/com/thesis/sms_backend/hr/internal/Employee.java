package com.thesis.sms_backend.hr.internal;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "employees", schema = "hr")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee {

    public enum Role {
        ADMIN, MANAGER, TEACHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", updatable = false, nullable = false, columnDefinition = "UUID")
    @Setter(AccessLevel.NONE)
    private UUID uuid;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Builder
    private Employee(String firstName, String lastName, String middleName,
                     Role role, boolean active, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.role = role;
        this.active = active;
        this.email = email;
        this.phone = phone;
    }
}