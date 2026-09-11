package com.uisrael.backend.service;

import com.uisrael.backend.dto.LoginRequestDTO;
import com.uisrael.backend.dto.LoginResponseDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO request);
}
