package com.example.Integration.project.controller;

import com.example.Integration.project.entity.User;
import org.springframework.web.bind.annotation.*;
import com.example.Integration.project.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/all")
    public List<User> getAll() {
        return repo.findAll();
    }

    @PostMapping("/add")
    public User create(@RequestBody User u) {
        return repo.save(u);
    }
}


