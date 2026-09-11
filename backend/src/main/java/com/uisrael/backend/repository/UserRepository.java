package com.uisrael.backend.repository;

import com.uisrael.backend.entity.TblUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<TblUser, Long> {

    Optional<TblUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
