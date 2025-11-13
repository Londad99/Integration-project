package com.example.Integration.project.service;

import com.example.Integration.project.dto.*;
import com.example.Integration.project.entity.*;
import com.example.Integration.project.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudyPlanService {
    private final StudyPlanRepository studyPlanRepo;
    private final PlanEntryRepository planEntryRepo;
    private final PlanEntryTopicRepository planEntryTopicRepo;
    private final UserRepository userRepo;
    private final CurriculumRepository curriculumRepo;
    private final TopicRepository topicRepo;

    public StudyPlanService(
            StudyPlanRepository studyPlanRepo,
            PlanEntryRepository planEntryRepo,
            PlanEntryTopicRepository planEntryTopicRepo,
            UserRepository userRepo,
            CurriculumRepository curriculumRepo,
            TopicRepository topicRepo
    ) {
        this.studyPlanRepo = studyPlanRepo;
        this.planEntryRepo = planEntryRepo;
        this.planEntryTopicRepo = planEntryTopicRepo;
        this.userRepo = userRepo;
        this.curriculumRepo = curriculumRepo;
        this.topicRepo = topicRepo;
    }

    public List<StudyPlan> getAll() {
        return studyPlanRepo.findAll();
    }

    public StudyPlan getById(Long id) {
        return studyPlanRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("StudyPlan not found"));
    }

    @Transactional
    public StudyPlan createWithEntries(StudyPlanDTO dto) {
        User creator = userRepo.findById(dto.getCreatedById())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Curriculum curriculum = curriculumRepo.findById(dto.getCurriculumId())
                .orElseThrow(() -> new RuntimeException("Curriculum not found"));

        StudyPlan plan = new StudyPlan();
        plan.setTitle(dto.getTitle());
        plan.setAdminNumber(dto.getAdminNumber());
        plan.setSchedule(dto.getSchedule());
        plan.setNotes(dto.getNotes());
        plan.setCreatedAt(OffsetDateTime.now());
        plan.setCreatedBy(creator);
        plan.setCurriculum(curriculum);

        StudyPlan savedPlan = studyPlanRepo.save(plan);

        if (dto.getEntries() != null) {
            for (PlanEntryDTO entryDto : dto.getEntries()) {
                PlanEntry entry = new PlanEntry();
                entry.setStudyPlan(savedPlan);
                entry.setDate(LocalDate.parse(entryDto.getDate()));
                entry.setCreatedAt(OffsetDateTime.now());
                PlanEntry savedEntry = planEntryRepo.save(entry);

                if (entryDto.getTopics() != null) {
                    for (PlanEntryTopicDTO topicDto : entryDto.getTopics()) {
                        Topic topic = topicRepo.findById(topicDto.getTopicId())
                                .orElseThrow(() -> new RuntimeException("Topic not found"));

                        PlanEntryTopic pet = new PlanEntryTopic();
                        pet.setId(new PlanEntryTopicId(savedEntry.getId(), topic.getId()));
                        pet.setPlanEntry(savedEntry);
                        pet.setTopic(topic);
                        pet.setDescription(topicDto.getDescription());

                        planEntryTopicRepo.save(pet);
                    }
                }
            }
        }

        return savedPlan;
    }

    public void delete(Long id) {
        studyPlanRepo.deleteById(id);
    }

    public StudyPlan save(StudyPlan plan) {
        return studyPlanRepo.save(plan);
    }
}
