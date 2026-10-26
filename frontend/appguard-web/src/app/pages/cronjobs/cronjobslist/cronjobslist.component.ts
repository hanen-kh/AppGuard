import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CronJob } from '../../../models/cronjob';
import { CronjobService } from '../../../services/cronjobservice/cronjob.service';
import { TagService } from '../../../services/tagservice/tag.service';

import { Tag, TagType } from '../../../models/tag';
@Component({
  selector: 'app-cronjobslist',
  standalone: false,
  templateUrl: './cronjobslist.component.html',
  styleUrl: './cronjobslist.component.css'
})
export class CronjobslistComponent implements OnInit {


 showAddTagForm = false;
  allTags: Tag[] = [];
 selectedCronId!: number; // ID du cron job pour lequel on ajoute des tags
showTagList = false;
  cronJobs: CronJob[] = [];
  filteredJobs: CronJob[] = [];
  selectedJobs: Set<number> = new Set();
  allSelected = false;
  dropdownOpen = false;

  filters = { tag: '', status: '', server: '' };
  
  allServers: string[] = [];
   showAddCronJob = false;

  constructor(private cronJobService: CronjobService, private tagService: TagService) {}


 
 loadAllTags(): void {
    this.tagService.getAllTags().subscribe({
      next: (tags) => this.allTags = tags,
      error: (err) => console.error('Error loading tags', err)
    });
  }
 

 openTagList(cronId: number): void {
  this.selectedCronId = cronId;
  this.showTagList = true;
}


 // Ouvrir le modal pour ajouter un tag
  openAddTag() {
    this.showAddTagForm = true;
  }

  // Fermer le modal
  closeAddTag() {
    this.showAddTagForm = false;
  }

  // Recevoir le tag créé et l’ajouter à la liste
  onTagCreated(tag: Tag) {
    this.allTags.push(tag);
    this.closeAddTag();
  }


  ngOnInit(): void {
    this.loadCronJobs();
    this.loadAllTags();
  }

  loadCronJobs(): void {
    this.cronJobService.getAllCronJobs().subscribe({
      next: (data) => {
        this.cronJobs = data;
        this.filteredJobs = [...data];
        
      },
      error: (err) => console.error('Error loading cron jobs:', err)
    });
  }

  

  applyFilters(): void {
    this.filteredJobs = this.cronJobs.filter(job => {
      const matchesTag = !this.filters.tag || job.tags.includes(this.filters.tag);
      const matchesStatus = !this.filters.status || job.status === this.filters.status;
      const matchesServer = !this.filters.server || job.serverName === this.filters.server;
      return matchesTag && matchesStatus && matchesServer;
    });
    this.selectedJobs.clear();
    this.allSelected = false;
  }

 toggleSelection(jobId: number, event: Event): void {
  const input = event.target as HTMLInputElement;
  const isChecked = input.checked;

  if (isChecked) {
    this.selectedJobs.add(jobId);
  } else {
    this.selectedJobs.delete(jobId);
  }
}


  toggleAll(event: any): void {
    const checked = event.target.checked;
    this.allSelected = checked;
    this.selectedJobs.clear();
    if (checked) {
      this.filteredJobs.forEach(job => this.selectedJobs.add(job.id));
    }
  }

  toggleDropdown(): void {
    this.dropdownOpen = !this.dropdownOpen;
  }

  deactivateSelected(): void {
    this.filteredJobs.forEach(job => {
      if (this.selectedJobs.has(job.id)) job.status = 'INACTIVE';
    });
    this.clearSelection();
  }

  activateSelected(): void {
    this.filteredJobs.forEach(job => {
      if (this.selectedJobs.has(job.id)) job.status = 'ACTIVE';
    });
    this.clearSelection();
  }

  deleteSelected(): void {
    if (!confirm(`Delete ${this.selectedJobs.size} selected jobs?`)) return;
    this.cronJobs = this.cronJobs.filter(job => !this.selectedJobs.has(job.id));
    this.applyFilters();
    this.clearSelection();
  }

  clearSelection(): void {
    this.selectedJobs.clear();
    this.allSelected = false;
    this.dropdownOpen = false;
  }

  getTagClass(tag: string): string {
    const typeMap: any = {
      'prod': 'tag-environment',
      'dev': 'tag-environment',
      'staging': 'tag-environment',
      'backup': 'tag-type',
      'maintenance': 'tag-type',
      'business': 'tag-type',
      'high': 'tag-priority',
      'medium': 'tag-priority',
      'low': 'tag-priority',
      'client-a': 'tag-client',
      'client-b': 'tag-client'
    };
    return typeMap[tag] || 'tag-custom';
  }


showAddCronForm = false;

addCronJob(): void {
  this.showAddCronForm = true; // Affiche le formulaire
}

closeAddCronForm(): void {
  this.showAddCronForm = false; // Ferme le formulaire
}

// Quand un cron job est ajouté depuis AddCronJobComponent
onCronJobAdded(cronJob: CronJob): void {
  this.cronJobs.push(cronJob);
  this.filteredJobs = [...this.cronJobs];
  this.closeAddCronForm();
}

getSelectedCronId(): number  {

    return Array.from(this.selectedJobs)[0]; // récupère le seul ID
  
 
}

}