package com.sfm.projetPFE.CronJob.Tag;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired private TagServiceImpl tagService;

    // ➡️ Créer un tag
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        return ResponseEntity.ok(tagService.createTag(tag));
    }

    // ➡️ Récupérer tous les tags
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    // ➡️ Récupérer un tag par ID
    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    // ➡️ Récupérer un tag par nom
    @GetMapping("/by-name/{name}")
    public ResponseEntity<Tag> getTagByName(@PathVariable String name) {
        return ResponseEntity.ok(tagService.getTagByName(name));
    }

    // ➡️ Mettre à jour un tag
    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        return ResponseEntity.ok(tagService.updateTag(id, tag));
    }

    // ➡️ Supprimer un tag
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    // ➡️ Associer un tag à un cron job
    @PostMapping("/{tagId}/cronjobs/{cronJobId}")
    public ResponseEntity<Void> addTagToCronJob(
            @PathVariable Long tagId,
            @PathVariable Long cronJobId) {
        tagService.addTagToCronJob(tagId, cronJobId);
        return ResponseEntity.ok().build();
    }

    // ➡️ Gestion des erreurs
    @ExceptionHandler({IllegalArgumentException.class, EntityNotFoundException.class})
    public ResponseEntity<String> handleValidationExceptions(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
