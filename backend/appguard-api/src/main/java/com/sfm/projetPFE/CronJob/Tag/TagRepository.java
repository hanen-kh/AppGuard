package com.sfm.projetPFE.CronJob.Tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);
    boolean existsByName(String name);

    boolean existsByNameAndType(String name, TagType type);

    boolean existsByNameAndTypeAndIdNot(String name, TagType type, Long id);
}
