package com.uisrael.backend.service.impl;

import com.uisrael.backend.dto.RolCreateDTO;
import com.uisrael.backend.dto.RolDTO;
import com.uisrael.backend.dto.RolUpdateDTO;
import com.uisrael.backend.entity.TblRol;
import com.uisrael.backend.exception.ApiException;
import com.uisrael.backend.repository.RolRepository;
import com.uisrael.backend.service.RolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RolDTO> listar() {
        log.info("Listando roles");
        return rolRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RolDTO obtenerPorId(Long id) {
        return toDTO(buscarPorId(id));
    }

    @Override
    @Transactional
    public RolDTO crear(RolCreateDTO dto) {
        if (rolRepository.existsByNombre(dto.getNombre())) {
            throw ApiException.conflict("El rol '" + dto.getNombre() + "' ya existe");
        }
        TblRol rol = TblRol.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();
        TblRol guardado = rolRepository.save(rol);
        log.info("Rol creado: {} (id={})", guardado.getNombre(), guardado.getIdRol());
        return toDTO(guardado);
    }

    @Override
    @Transactional
    public RolDTO actualizar(Long id, RolUpdateDTO dto) {
        TblRol rol = buscarPorId(id);
        rol.setNombre(dto.getNombre());
        rol.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) {
            rol.setActivo(dto.getActivo());
        }
        TblRol actualizado = rolRepository.save(rol);
        log.info("Rol actualizado: {} (id={})", actualizado.getNombre(), actualizado.getIdRol());
        return toDTO(actualizado);
    }

    private TblRol buscarPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Rol no encontrado con id " + id));
    }

    private RolDTO toDTO(TblRol rol) {
        return RolDTO.builder()
                .idRol(rol.getIdRol())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .activo(rol.getActivo())
                .fechaCreacion(rol.getFechaCreacion())
                .build();
    }
}
