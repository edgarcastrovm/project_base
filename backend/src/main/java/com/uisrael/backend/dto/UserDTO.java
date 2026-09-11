package com.uisrael.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de salida (respuesta) para tbl_user. Nunca expone el password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long idUser;
    private String username;
    private String email;
    private String nombres;
    private String apellidos;
    private Boolean activo;
    private Long idRol;
    private String rolNombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
