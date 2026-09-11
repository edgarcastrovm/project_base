package com.uisrael.backend.repository;

import com.uisrael.backend.entity.TblRol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<TblRol, Long> {

    boolean existsByNombre(String nombre);
}
