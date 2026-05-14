package com.br3akPoint.auth_service.controller;

import com.br3akPoint.auth_service.data.DeviceContext;
import com.br3akPoint.auth_service.data.dto.request.AuthRequestDTO;
import com.br3akPoint.auth_service.data.dto.response.LoginAuthDTO;
import com.br3akPoint.auth_service.data.dto.response.RefreshTokenDTO;
import com.br3akPoint.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import response.ApiResponse;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody AuthRequestDTO dto, HttpServletRequest request) throws Exception {
        DeviceContext deviceContext = (DeviceContext) request.getAttribute("device_context");
        LoginAuthDTO authDTO = authService.loginUser(dto.getEmail(), dto.getPassword(), deviceContext);
        return ResponseEntity.ok(ApiResponse.responseData(authDTO));
    }

    @PutMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody AuthRequestDTO dto) throws Exception {
        authService.registerUser(dto.getEmail(), dto.getPassword());
        return ResponseEntity.ok(ApiResponse.statusOk());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenDTO>> refreshAuthToken(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        DeviceContext deviceContext = (DeviceContext) request.getAttribute("device_context");
        String refreshToken = (String) body.get("refresh_token");
        var newToken = authService.refreshAuthToken(refreshToken, deviceContext);
        return ResponseEntity.ok(ApiResponse.responseData(newToken));
    }
}
