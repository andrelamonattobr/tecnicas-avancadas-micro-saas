package com.msaas.back.comerciante.presentation;

import com.msaas.back.comerciante.application.ComercianteAuthService;
import com.msaas.back.comerciante.application.dto.AuthResponse;
import com.msaas.back.comerciante.application.dto.LoginRequest;
import com.msaas.back.comerciante.application.dto.RegistroComercianteRequest;
import com.msaas.back.comerciante.application.dto.RegistroComercianteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ComercianteAuthService comercianteAuthService;

    public AuthController(ComercianteAuthService comercianteAuthService) {
        this.comercianteAuthService = comercianteAuthService;
    }

    @PostMapping("/registro")
    public ResponseEntity<RegistroComercianteResponse> registrar(@RequestBody RegistroComercianteRequest request) {
        RegistroComercianteResponse response = comercianteAuthService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = comercianteAuthService.autenticar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("mensagem", "Sessão encerrada com sucesso"));
    }
}
