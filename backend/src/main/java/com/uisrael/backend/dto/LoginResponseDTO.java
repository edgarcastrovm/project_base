package com.uisrael.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
    private String tokenType;
    private Long idUser;
    private String username;
    private String email;
    private String nombreCompleto;
    private String rol;
}
