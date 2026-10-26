import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { Tag, TagType } from '../../../../models/tag';
import { TagService } from '../../../../services/tagservice/tag.service';
@Component({
  selector: 'app-taglist',
  standalone: false,
  templateUrl: './taglist.component.html',
  styleUrl: './taglist.component.css'
})
export class TaglistComponent implements OnInit {
  allTags: Tag[] = [];

  @Input() cronJobId!: number; // ID du cron job actuel
  @Output() tagAdded = new EventEmitter<Tag>(); // Émettre après ajout
  @Output() close = new EventEmitter<void>(); // Define the close property as an @Output()

  constructor(private tagService: TagService) {}

  ngOnInit(): void {
    this.loadTags();
  }

  loadTags(): void {
    this.tagService.getAllTags().subscribe({
      next: tags => this.allTags = tags,
      error: err => console.error('Error loading tags', err)
    });
  }

  addTagToCron(tag: Tag): void {
    if (!this.cronJobId) return;

    this.tagService.addTagToCronJob(tag.id!, this.cronJobId).subscribe({
      next: () => {
        console.log(`Tag ${tag.name} added to cron ${this.cronJobId}`);
        this.tagAdded.emit(tag);
      },
      error: err => console.error('Error adding tag', err)
    });
  }
 getTagClass(type: string): string {
  switch(type) {
    case 'ENVIRONMENT':
      return 'tag-environment';
    case 'CLIENT':
      return 'tag-client';
    case 'TYPE':
      return 'tag-type';
    case 'PRIORITY':
      return 'tag-priority';
    case 'CUSTOM':
    default:
      return 'tag-custom';
  }
}

  closeModal(): void {
    this.close.emit();
  }

}

