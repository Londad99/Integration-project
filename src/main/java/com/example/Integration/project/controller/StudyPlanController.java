package com.example.Integration.project.controller;

import com.example.Integration.project.dto.StudyPlanDTO;
import com.example.Integration.project.entity.StudyPlan;
import com.example.Integration.project.service.StudyPlanService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/study-plans")
@CrossOrigin(origins = "*")
public class StudyPlanController {
    private final StudyPlanService service;

    public StudyPlanController(StudyPlanService service) {
        this.service = service;
    }

    @GetMapping("/all")
    public List<StudyPlan> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public StudyPlan getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/create")
    public StudyPlan createWithEntries(@RequestBody StudyPlanDTO dto) {
        return service.createWithEntries(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
