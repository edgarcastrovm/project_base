package com.uisrael.backend.service.impl;

import com.uisrael.backend.dto.UserCreateDTO;
import com.uisrael.backend.dto.UserDTO;
import com.uisrael.backend.dto.UserUpdateDTO;
import com.uisrael.backend.entity.TblRol;
import com.uisrael.backend.entity.TblUser;
import com.uisrael.backend.exception.ApiException;
import com.uisrael.backend.repository.RolRepository;
import com.uisrael.backend.repository.UserRepository;
import com.uisrael.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> listar() {
        log.info("Listando usuarios");
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO obtenerPorId(Long id) {
        TblUser user = buscarPorId(id);
        return toDTO(user);
    }

    @Override
    @Transactional
    public UserDTO crear(UserCreateDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw ApiException.conflict("El username '" + dto.getUsername() + "' ya está en uso");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw ApiException.conflict("El email '" + dto.getEmail() + "' ya está registrado");
        }

        TblRol rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> ApiException.notFound("Rol no encontrado con id " + dto.getIdRol()));

        TblUser user = TblUser.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .rol(rol)
                .activo(true)
                .build();

        TblUser guardado = userRepository.save(user);
        log.info("Usuario creado: {} (id={})", guardado.getUsername(), guardado.getIdUser());
        return toDTO(guardado);
    }

    @Override
    @Transactional
    public UserDTO actualizar(Long id, UserUpdateDTO dto) {
        TblUser user = buscarPorId(id);

        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw ApiException.conflict("El email '" + dto.getEmail() + "' ya está registrado");
        }

        TblRol rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> ApiException.notFound("Rol no encontrado con id " + dto.getIdRol()));

        user.setEmail(dto.getEmail());
        user.setNombres(dto.getNombres());
        user.setApellidos(dto.getApellidos());
        user.setRol(rol);
        if (dto.getActivo() != null) {
            user.setActivo(dto.getActivo());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        TblUser actualizado = userRepository.save(user);
        log.info("Usuario actualizado: {} (id={})", actualizado.getUsername(), actualizado.getIdUser());
        return toDTO(actualizado);
    }

    private TblUser buscarPorId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado con id " + id));
    }

    private UserDTO toDTO(TblUser user) {
        return UserDTO.builder()
                .idUser(user.getIdUser())
                .username(user.getUsername())
                .email(user.getEmail())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .activo(user.getActivo())
                .idRol(user.getRol() != null ? user.getRol().getIdRol() : null)
                .rolNombre(user.getRol() != null ? user.getRol().getNombre() : null)
                .fechaCreacion(user.getFechaCreacion())
                .fechaActualizacion(user.getFechaActualizacion())
                .build();
    }
}
