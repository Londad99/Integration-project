package com.example.Integration.project.controller;

import com.example.Integration.project.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService svc;

    public UserController(UserService svc) {
        this.svc = svc;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            Object users = svc.getAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener usuarios");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String,String> body) {
        String name = body.getOrDefault("name","");
        String email = body.getOrDefault("email","");

        String result = svc.register(name, email);
        return switch (result) {
            case "EXISTS_VERIFIED" -> ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está registrado y verificado.");
            case "EXISTS_NOT_VERIFIED" -> ResponseEntity.ok("El usuario existe pero no está verificado. Se ha enviado un nuevo código.");
            case "CONFLICT" -> ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está registrado.");
            case "SENT" -> ResponseEntity.ok("Código enviado al correo: " + email);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado");
        };
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String email, @RequestParam String code) {
        String result = svc.verify(email, code);
        return switch (result) {
            case "INVALID_OR_EXPIRED" -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código inválido o expirado.");
            case "ALREADY_VERIFIED" -> ResponseEntity.ok("Cuenta ya verificada.");
            case "VERIFIED_AND_SENT_PROVISIONAL" -> ResponseEntity.ok("Cuenta verificada. Se ha enviado una contraseña provisional al correo.");
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado");
        };
    }

    @PostMapping("/set-password")
    public ResponseEntity<String> setPassword(@RequestBody Map<String,String> body) {
        String email = body.getOrDefault("email","");
        String provisional = body.getOrDefault("provisional","");
        String newPassword = body.getOrDefault("newPassword","");

        if (newPassword.length() < 6) return ResponseEntity.badRequest().body("La nueva contraseña debe tener al menos 6 caracteres.");

        String result = svc.setPassword(email, provisional, newPassword);
        return switch (result) {
            case "NOT_FOUND" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
            case "PROVISIONAL_INVALID" -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Contraseña provisional inválida");
            case "PASSWORD_UPDATED" -> ResponseEntity.ok("Contraseña actualizada correctamente");
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado");
        };
    }
}


