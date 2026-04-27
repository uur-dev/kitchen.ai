package com.br3akPoint.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/auth/testing")
    public String testAuth() {
        return "working...";
    }
}
