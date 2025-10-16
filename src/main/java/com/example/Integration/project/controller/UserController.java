package com.example.Integration.project.controller;

import com.example.Integration.project.entity.User;
import com.example.Integration.project.service.MailService;
import com.example.Integration.project.service.VerificationService;
import org.springframework.web.bind.annotation.*;
import com.example.Integration.project.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository repo;
    private final VerificationService verificationService;
    private final MailService mailService;

    public UserController(UserRepository repo, VerificationService verificationService, MailService mailService) {
        this.repo = repo;
        this.verificationService = verificationService;
        this.mailService = mailService;
    }

    @GetMapping("/all")
    public List<User> getAll() {
        return repo.findAll();
    }

    @PostMapping("/add")
    public User create(@RequestBody User u) {
        return repo.save(u);
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {

        Optional<Object> existing = repo.findByEmail(user.getEmail());

        if (existing.isPresent()) {
            User u = (User) existing.get();
            if (u.isVerified()) {
                return "El correo ya está registrado y verificado.";
            } else {
                String code = verificationService.generateCode(u.getEmail());
                mailService.sendVerificationEmail(u.getEmail(), code);
                return "El usuario ya existe pero no estaba verificado. Se ha enviado un nuevo código al correo: " + u.getEmail();
            }
        }

        user.setVerified(false);
        repo.save(user);

        String code = verificationService.generateCode(user.getEmail());
        mailService.sendVerificationEmail(user.getEmail(), code);

        return "Código enviado al correo: " + user.getEmail();
    }

    @PostMapping("/verify")
    public String verify(@RequestParam String email, @RequestParam String code) {
        boolean ok = verificationService.verifyCode(email, code);
        if (!ok) return "Código inválido o expirado.";

        User user = (User) repo.findByEmail(email).orElseThrow();
        user.setVerified(true);
        repo.save(user);
        return "Cuenta verificada correctamente";
    }
}


