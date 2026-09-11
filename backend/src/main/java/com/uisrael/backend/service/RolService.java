package com.uisrael.backend.service;

import com.uisrael.backend.dto.RolCreateDTO;
import com.uisrael.backend.dto.RolDTO;
import com.uisrael.backend.dto.RolUpdateDTO;

import java.util.List;

public interface RolService {

    List<RolDTO> listar();

    RolDTO obtenerPorId(Long id);

    RolDTO crear(RolCreateDTO dto);

    RolDTO actualizar(Long id, RolUpdateDTO dto);
}
