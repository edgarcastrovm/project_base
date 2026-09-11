package com.uisrael.backend.config;

import com.uisrael.backend.entity.TblRol;
import com.uisrael.backend.entity.TblUser;
import com.uisrael.backend.repository.RolRepository;
import com.uisrael.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga datos base (roles y usuario administrador) al iniciar la aplicación,
 * solo si la base de datos está vacía. Facilita probar el login sin pasos
 * manuales adicionales.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class DataSeeder implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        TblRol rolAdmin = rolRepository.findAll().stream()
                .filter(r -> "ADMIN".equalsIgnoreCase(r.getNombre()))
                .findFirst()
                .orElseGet(() -> rolRepository.save(TblRol.builder()
                        .nombre("ADMIN")
                        .descripcion("Administrador del sistema, acceso total")
                        .activo(true)
                        .build()));

        rolRepository.findAll().stream()
                .filter(r -> "USER".equalsIgnoreCase(r.getNombre()))
                .findFirst()
                .orElseGet(() -> rolRepository.save(TblRol.builder()
                        .nombre("USER")
                        .descripcion("Usuario estándar")
                        .activo(true)
                        .build()));

        if (!userRepository.existsByUsername("admin")) {
            TblUser admin = TblUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin123"))
                    .email("admin@uisrael.local")
                    .nombres("Administrador")
                    .apellidos("Sistema")
                    .rol(rolAdmin)
                    .activo(true)
                    .build();
            userRepository.save(admin);
            log.info("Usuario administrador semilla creado: admin / Admin123");
        }
    }
}
