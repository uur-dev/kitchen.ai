package com.br3akPoint.auth_service.controller;

import com.br3akPoint.auth_service.data.dto.request.AuthRequestDTO;
import com.br3akPoint.auth_service.data.dto.response.LoginAuthDTO;
import com.br3akPoint.auth_service.service.AuthService;
import com.br3akPoint.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody AuthRequestDTO dto, HttpServletRequest request) throws Exception {
        String deviceType = (String) request.getAttribute("device_type");
        LoginAuthDTO authDTO = authService.loginUser(dto.getEmail(), dto.getPassword(), deviceType);
        return ResponseEntity.ok(ApiResponse.responseData(authDTO));
    }

    @PutMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody AuthRequestDTO dto) throws Exception {
        authService.registerUser(dto.getEmail(), dto.getPassword());
        return ResponseEntity.ok(ApiResponse.statusOk());
    }
}
