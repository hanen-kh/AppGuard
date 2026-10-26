import { Component, EventEmitter, Output } from '@angular/core';
import { TagService } from '../../../../services/tagservice/tag.service';
import {Tag,TagType} from '../../../../models/tag';

@Component({
  selector: 'app-tag',
  standalone: false,
  templateUrl: './tag.component.html',
  styleUrl: './tag.component.css'
})
export class TagComponent {
 newTag: Tag = { name: '', type: TagType.CUSTOM };
  isSubmitting = false;
  message = '';

  @Output() tagCreated = new EventEmitter<Tag>();
  @Output() close = new EventEmitter<void>();

  constructor(private tagService: TagService) {}

  // Méthode appelée au clic "Create Tag"
  createTag() {
    if (!this.newTag.name.trim()) return;

    this.isSubmitting = true;
    this.tagService.createTag(this.newTag).subscribe({
      next: (tag) => {
        this.tagCreated.emit(tag); // informe le parent
        this.isSubmitting = false;
        this.close.emit();         // ferme le modal
      },
      error: () => {
        this.isSubmitting = false;
        // éventuellement gérer une erreur ici
      }
    });
  }

  closeForm() {
    this.close.emit();
  }

  getTagClass(): string {
    switch (this.newTag.type) {
      case TagType.ENVIRONMENT: return 'tag-environment';
      case TagType.CLIENT: return 'tag-client';
      case TagType.TYPE: return 'tag-type';
      case TagType.PRIORITY: return 'tag-priority';
      case TagType.CUSTOM: return 'tag-custom';
      default: return '';
    }
  }
}