package com.example.Integration.project.controller;

import com.example.Integration.project.entity.Curriculum;
import org.springframework.web.bind.annotation.*;
import com.example.Integration.project.repository.CurriculumRepository;

import java.util.List;

@RestController
@RequestMapping("/curriculums")
public class CurriculumController {
    private final CurriculumRepository repo;

    public CurriculumController(CurriculumRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Curriculum> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Curriculum create(@RequestBody Curriculum c) {
        return repo.save(c);
    }
}

