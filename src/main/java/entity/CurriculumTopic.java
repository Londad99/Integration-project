package entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "curriculum_topics")
public class CurriculumTopic {
    @EmbeddedId
    private CurriculumTopicId id;

    @ManyToOne
    @MapsId("curriculumId")
    @JoinColumn(name = "curriculum_id")
    private Curriculum curriculum;

    @ManyToOne
    @MapsId("topicId")
    @JoinColumn(name = "topic_id")
    private Topic topic;

    private Short position;
}

