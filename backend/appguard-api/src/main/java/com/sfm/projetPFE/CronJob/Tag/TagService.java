package com.sfm.projetPFE.CronJob.Tag;

import java.util.List;

public interface TagService {


    Tag createTag(Tag tag);
    List<Tag> getAllTags();
    Tag getTagById(Long id);
    Tag getTagByName(String name);
    Tag updateTag(Long id, Tag updatedTag);
    void deleteTag(Long id);
    void addTagToCronJob(Long tagId, Long cronJobId);
}
