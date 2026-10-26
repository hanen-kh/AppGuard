import { Component, HostListener, OnInit  } from '@angular/core';
import { CronExecution } from '../../../models/cronExecution';
import { CronexecutionService } from '../../../services/cronexecutionservice/cronexecution.service';
@Component({
  selector: 'app-cronhistory',
  standalone: false,
  templateUrl: './cronhistory.component.html',
  styleUrl: './cronhistory.component.css'
})
export class CronhistoryComponent implements OnInit {
  dropdownOpen = false;
  executions: CronExecution[] = [];
  filteredExecutions: CronExecution[] = [];
  currentFilter = 'ALL';
  selectedExecutions = new Set<number>();
  allSelected = false;

  constructor(private executionService: CronexecutionService) {}

  ngOnInit(): void { this.loadExecutions(); }

  @HostListener('document:click', ['$event'])
  closeOnOutsideClick(event: Event) {
    const target = event.target as HTMLElement;
    // ferme le menu si on clique en dehors du .dropdown
    if (!target.closest('.dropdown')) this.dropdownOpen = false;
  }

  toggleDropdown(): void { this.dropdownOpen = !this.dropdownOpen; }

  loadExecutions(): void {
    this.executionService.getAllExecutions().subscribe({
      next: (data) => { this.executions = data; this.applyFilter(); },
      error: (err) => console.error('Error loading executions:', err)
    });
  }

  applyFilter(): void {
    this.filteredExecutions = this.currentFilter === 'ALL'
      ? [...this.executions]
      : this.executions.filter(exec => exec.status === this.currentFilter);
    this.resetSelection();
  }

  setFilter(status: string): void {
    this.currentFilter = status;
    this.applyFilter();
    this.dropdownOpen = false; // ferme après sélection
  }

  toggleSelection(id: number, checked: boolean): void {
    if (checked) this.selectedExecutions.add(id);
    else { this.selectedExecutions.delete(id); this.allSelected = false; }
  }

  toggleAllSelections(): void {
    if (this.allSelected) { this.selectedExecutions.clear(); this.allSelected = false; }
    else { this.filteredExecutions.forEach(e => this.selectedExecutions.add(e.id!)); this.allSelected = true; }
  }

  resetSelection(): void { this.selectedExecutions.clear(); this.allSelected = false; }

  deleteSelected(): void {
    if (!this.selectedExecutions.size) return;
    if (confirm(`Are you sure you want to delete ${this.selectedExecutions.size} execution(s)?`)) {
      this.executions = this.executions.filter(e => !this.selectedExecutions.has(e.id!));
      this.applyFilter();
      alert('Selected executions have been deleted successfully.');
    }
  }

  toggleSelectionFromEvent(execId: number, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.toggleSelection(execId, checked);
  }

  getStatusIcon(status: string | null): string {
    switch (status) {
      case 'RUNNING': return 'fa-play';
      case 'SUCCESS': return 'fa-check';
      case 'FAILURE': return 'fa-times';
      case 'TIMEOUT': return 'fa-clock';
      case 'CANCELED': return 'fa-ban';
      case 'NOT_EXECUTED': return 'fa-minus';
      case 'AGENT_UNREACHABLE': return 'fa-exclamation-triangle';
      default: return 'fa-question';
    }
  }
}