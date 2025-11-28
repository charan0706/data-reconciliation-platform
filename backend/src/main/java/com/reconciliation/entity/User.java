package com.reconciliation.entity;

import com.reconciliation.enums.UserRole;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity for authentication and authorization.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "department", length = 100)
    private String department;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    @Column(name = "is_locked")
    private Boolean isLocked = false;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @ManyToMany
    @JoinTable(
        name = "user_systems",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "system_id")
    )
    private Set<SourceSystem> accessibleSystems = new HashSet<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

