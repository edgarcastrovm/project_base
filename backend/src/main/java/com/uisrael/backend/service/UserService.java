package com.uisrael.backend.service;

import com.uisrael.backend.dto.UserCreateDTO;
import com.uisrael.backend.dto.UserDTO;
import com.uisrael.backend.dto.UserUpdateDTO;

import java.util.List;

public interface UserService {

    List<UserDTO> listar();

    UserDTO obtenerPorId(Long id);

    UserDTO crear(UserCreateDTO dto);

    UserDTO actualizar(Long id, UserUpdateDTO dto);
}
