package controller;

import entity.Role;
import org.springframework.web.bind.annotation.*;
import repository.RoleRepository;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {
    private final RoleRepository repo;

    public RoleController(RoleRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Role> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Role create(@RequestBody Role r) {
        return repo.save(r);
    }
}

