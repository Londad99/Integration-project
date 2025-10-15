package entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plan_entry_topics")
public class PlanEntryTopic {
    @EmbeddedId
    private PlanEntryTopicId id;

    @ManyToOne
    @MapsId("planEntryId")
    @JoinColumn(name = "plan_entry_id")
    private PlanEntry planEntry;

    @ManyToOne
    @MapsId("topicId")
    @JoinColumn(name = "topic_id")
    private Topic topic;

    private String description;
}

