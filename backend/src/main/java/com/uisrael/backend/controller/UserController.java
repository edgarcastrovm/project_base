package com.uisrael.backend.controller;

import com.uisrael.backend.dto.UserCreateDTO;
import com.uisrael.backend.dto.UserDTO;
import com.uisrael.backend.dto.UserUpdateDTO;
import com.uisrael.backend.dto.response.ApiResponse;
import com.uisrael.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> listar() {
        List<UserDTO> usuarios = userService.listar();
        return ResponseEntity.ok(ApiResponse.ok("Usuarios listados correctamente", usuarios));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> obtener(@PathVariable Long id) {
        UserDTO usuario = userService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario encontrado", usuario));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> crear(@Valid @RequestBody UserCreateDTO dto) {
        UserDTO creado = userService.crear(dto);
        return ResponseEntity.status(201).body(ApiResponse.created("Usuario creado correctamente", creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> actualizar(@PathVariable Long id,
                                                             @Valid @RequestBody UserUpdateDTO dto) {
        UserDTO actualizado = userService.actualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado correctamente", actualizado));
    }
}
