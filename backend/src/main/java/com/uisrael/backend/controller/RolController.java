package com.uisrael.backend.controller;

import com.uisrael.backend.dto.RolCreateDTO;
import com.uisrael.backend.dto.RolDTO;
import com.uisrael.backend.dto.RolUpdateDTO;
import com.uisrael.backend.dto.response.ApiResponse;
import com.uisrael.backend.service.RolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RolDTO>>> listar() {
        List<RolDTO> roles = rolService.listar();
        return ResponseEntity.ok(ApiResponse.ok("Roles listados correctamente", roles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RolDTO>> obtener(@PathVariable Long id) {
        RolDTO rol = rolService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.ok("Rol encontrado", rol));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RolDTO>> crear(@Valid @RequestBody RolCreateDTO dto) {
        RolDTO creado = rolService.crear(dto);
        return ResponseEntity.status(201).body(ApiResponse.created("Rol creado correctamente", creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RolDTO>> actualizar(@PathVariable Long id,
                                                            @Valid @RequestBody RolUpdateDTO dto) {
        RolDTO actualizado = rolService.actualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Rol actualizado correctamente", actualizado));
    }
}
