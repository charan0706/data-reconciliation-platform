package com.reconciliation.dto;

import com.reconciliation.enums.UserRole;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String department;
    private Set<UserRole> roles;
    private Boolean isLocked;
    private Boolean isActive;
    private String createdAt;
}

