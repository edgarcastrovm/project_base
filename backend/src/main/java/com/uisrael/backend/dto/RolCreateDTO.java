package com.uisrael.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolCreateDTO {

    @NotBlank(message = "El nombre del rol es obligatorio")
    private String nombre;

    private String descripcion;
}
