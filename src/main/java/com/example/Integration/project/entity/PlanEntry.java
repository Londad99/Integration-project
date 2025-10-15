package com.example.Integration.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plan_entries")
public class PlanEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlan studyPlan;

    @Column(nullable = false)
    private LocalDate date;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "planEntry")
    private List<PlanEntryTopic> topics;
}
