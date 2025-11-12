package com.example.Integration.project.service;

import com.example.Integration.project.dto.CurriculumDTO;
import com.example.Integration.project.dto.TopicDTO;
import com.example.Integration.project.entity.Curriculum;
import com.example.Integration.project.entity.CurriculumTopic;
import com.example.Integration.project.entity.CurriculumTopicId;
import com.example.Integration.project.entity.Topic;
import com.example.Integration.project.exception.ResourceNotFoundException;
import com.example.Integration.project.repository.CurriculumRepository;
import com.example.Integration.project.repository.CurriculumTopicRepository;
import com.example.Integration.project.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CurriculumService {
    private final CurriculumRepository curriculumRepo;
    private final TopicRepository topicRepo;
    private final CurriculumTopicRepository ctRepo;

    public CurriculumService(CurriculumRepository curriculumRepo, TopicRepository topicRepo, CurriculumTopicRepository ctRepo) {
        this.curriculumRepo = curriculumRepo;
        this.topicRepo = topicRepo;
        this.ctRepo = ctRepo;
    }

    public List<Curriculum> findAll() {
        return curriculumRepo.findAll();
    }

    public Curriculum findById(Long id) {
        return curriculumRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Curriculum no encontrado: " + id));
    }

    @Transactional
    public Curriculum create(CurriculumDTO dto) {
        Curriculum c = new Curriculum();
        c.setTitle(dto.title);
        c.setLevel(dto.level);
        // createdBy, etc. si querés mapear, hacelo aquí
        Curriculum saved = curriculumRepo.save(c);

        // 1) crear topics nuevos si vienen en payload
        List<Topic> createdTopics = new ArrayList<>();
        if (dto.topics != null && !dto.topics.isEmpty()) {
            for (TopicDTO tDto : dto.topics) {
                Topic t = new Topic();
                t.setTitle(tDto.title);
                t.setCategory(tDto.category);
                t.setSubtopics(tDto.subtopics);
                createdTopics.add(topicRepo.save(t));
            }
        }

        // 2) asociar topicIds (existentes)
        List<Topic> topicsToAttach = new ArrayList<>();
        if (dto.topicIds != null) {
            for (Long tid : dto.topicIds) {
                Topic t = topicRepo.findById(tid).orElseThrow(() -> new ResourceNotFoundException("Topic no encontrado: " + tid));
                topicsToAttach.add(t);
            }
        }

        topicsToAttach.addAll(createdTopics);

        short pos = 0;
        for (Topic topic : topicsToAttach) {
            CurriculumTopic ct = new CurriculumTopic();
            ct.setId(new CurriculumTopicId(saved.getId(), topic.getId()));
            ct.setCurriculum(saved);
            ct.setTopic(topic);
            ct.setPosition(pos++);
            ctRepo.save(ct);
        }

        return curriculumRepo.findById(saved.getId()).orElse(saved);
    }

    @Transactional
    public Curriculum update(Long id, CurriculumDTO dto) {
        Curriculum c = findById(id);
        if (dto.title != null) c.setTitle(dto.title);
        if (dto.level != null) c.setLevel(dto.level);
        curriculumRepo.save(c);


        return c;
    }

    @Transactional
    public void addTopicToCurriculum(Long curriculumId, Long topicId) {
        Curriculum c = findById(curriculumId);
        Topic t = topicRepo.findById(topicId).orElseThrow(() -> new ResourceNotFoundException("Topic no encontrado: " + topicId));

        short nextPos = 0;
        List<CurriculumTopic> existing = ctRepo.findAll();
        CurriculumTopic ct = new CurriculumTopic();
        ct.setId(new CurriculumTopicId(c.getId(), t.getId()));
        ct.setCurriculum(c);
        ct.setTopic(t);
        ct.setPosition(nextPos);
        ctRepo.save(ct);
    }

    @Transactional
    public void removeTopicFromCurriculum(Long curriculumId, Long topicId) {
        CurriculumTopicId id = new CurriculumTopicId(curriculumId, topicId);
        if (ctRepo.existsById(id)) {
            ctRepo.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Asociación curriculum-topic no encontrada");
        }
    }

    public void delete(Long id) {
        Curriculum c = findById(id);
        curriculumRepo.delete(c);
    }
}
