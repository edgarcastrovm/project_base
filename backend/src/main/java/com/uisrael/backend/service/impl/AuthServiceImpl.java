package com.uisrael.backend.service.impl;

import com.uisrael.backend.dto.LoginRequestDTO;
import com.uisrael.backend.dto.LoginResponseDTO;
import com.uisrael.backend.entity.TblUser;
import com.uisrael.backend.security.CustomUserDetails;
import com.uisrael.backend.security.JwtUtil;
import com.uisrael.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        TblUser user = principal.getUser();

        String rolNombre = user.getRol() != null ? user.getRol().getNombre() : null;

        String token = jwtUtil.generateToken(user.getUsername(), Map.of(
                "idUser", user.getIdUser(),
                "email", user.getEmail(),
                "rol", rolNombre != null ? rolNombre : ""
        ));

        log.info("Login exitoso para el usuario '{}'", user.getUsername());

        String nombreCompleto = ((user.getNombres() != null ? user.getNombres() : "") + " "
                + (user.getApellidos() != null ? user.getApellidos() : "")).trim();

        return LoginResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .idUser(user.getIdUser())
                .username(user.getUsername())
                .email(user.getEmail())
                .nombreCompleto(nombreCompleto)
                .rol(rolNombre)
                .build();
    }
}
