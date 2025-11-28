package com.reconciliation.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserDTO user;
}

