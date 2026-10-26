package com.sfm.projetPFE.CronJob.Tag;


import com.sfm.projetPFE.CronJob.Cron.CronJob;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sfm.projetPFE.CronJob.Cron.CronJobRepository;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CronJobRepository cronJobRepository;

    @Override
    @Transactional
    public Tag createTag(Tag tag) {
        // Valider le tag selon son type

        // Vérifier l'unicité du tag (name + type)
        if (tagRepository.existsByNameAndType(tag.getName(), tag.getType())) {
            throw new IllegalArgumentException("Tag with name " + tag.getName() + " and type " + tag.getType() + " already exists");
        }
        return tagRepository.save(tag);
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    public Tag getTagById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag with ID " + id + " not found"));
    }

    @Override
    public Tag getTagByName(String name) {
        return tagRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Tag with name " + name + " not found"));
    }

    @Override
    @Transactional
    public Tag updateTag(Long id, Tag updatedTag) {
        Tag existingTag = getTagById(id);
        // Mettre à jour les champs
        existingTag.setName(updatedTag.getName());
        existingTag.setType(updatedTag.getType());
        validateTag(existingTag);
        // Vérifier l'unicité
        if (tagRepository.existsByNameAndTypeAndIdNot(updatedTag.getName(), updatedTag.getType(), id)) {
            throw new IllegalArgumentException("Tag with name " + updatedTag.getName() + " and type " + updatedTag.getType() + " already exists");
        }
        return tagRepository.save(existingTag);
    }

    @Override
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addTagToCronJob(Long tagId, Long cronJobId) {
        Tag tag = getTagById(tagId);
        CronJob cronJob = cronJobRepository.findById(cronJobId)
                .orElseThrow(() -> new EntityNotFoundException("CronJob with ID " + cronJobId + " not found"));
        cronJob.getTags().add(tag);
        cronJobRepository.save(cronJob);
    }


    private void validateTag(Tag tag) {
        switch (tag.getType()) {
            case ENVIRONMENT:
                if (!Arrays.asList("dev", "prod", "staging").contains(tag.getName())) {
                    throw new IllegalArgumentException("Invalid environment tag: " + tag.getName());
                }
                break;
            case PRIORITY:
                if (!Arrays.asList("high", "medium", "low").contains(tag.getName())) {
                    throw new IllegalArgumentException("Invalid priority tag: " + tag.getName());
                }
                break;
            case TYPE:
                if (!Arrays.asList("backup", "maintenance", "business").contains(tag.getName())) {
                    throw new IllegalArgumentException("Invalid type tag: " + tag.getName());
                }
                break;
            case CUSTOM:
                // Validation légère pour CUSTOM (ex. : longueur max)
                if (tag.getName().length() > 50) {
                    throw new IllegalArgumentException("Custom tag name too long: " + tag.getName());
                }
                break;
        }
    }
}
