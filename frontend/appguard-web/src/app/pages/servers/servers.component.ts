import { Component , OnInit} from '@angular/core';
import { ServerService } from '../../services/serverservice/server.service';
import { ServerDto } from '../../models/Server';
import { NgClass } from '@angular/common';
import { NgModel} from '@angular/forms';
import { NgModule } from '@angular/core';







@Component({
  selector: 'app-servers',
  standalone: false,
  templateUrl: './servers.component.html',
  styleUrl: './servers.component.css'
})
export class ServersComponent implements OnInit {
  servers: ServerDto[] = [];
  filteredServers: ServerDto[] = [];
  selectedServers: ServerDto[] = [];
    showAlert = false;
  alertMessage = '';
  
  // Filtres et tri
  searchTerm = '';
  statusFilter = '';
  includeHidden = false;
  sortColumn = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';
  
  // Gestion du dropdown
  showManageDropdown = false;

  constructor(private serverService: ServerService) {}

  ngOnInit(): void {
    this.loadServers();
  }

  loadServers(): void {
    this.serverService.getAllServers().subscribe({
      next: (data) => {
        this.servers = data;
        this.filterServers();
      },
      error: (err) => console.error('Error loading servers:', err)
    });
  }

  refreshServers(): void {
    this.loadServers();
  }

  filterServers(): void {
    let result = [...this.servers];
    
    // Filtre par recherche
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(server => 
        server.name.toLowerCase().includes(term) ||
        server.os.toLowerCase().includes(term) ||
        server.client.toLowerCase().includes(term)
      );
    }
    
    // Filtre par statut
    if (this.statusFilter) {
      result = result.filter(server => server.status === this.statusFilter);
    }
    
    // Tri
    result.sort((a, b) => {
      const aValue = a[this.sortColumn as keyof ServerDto];
      const bValue = b[this.sortColumn as keyof ServerDto];
      
      if (aValue < bValue) return this.sortDirection === 'asc' ? -1 : 1;
      if (aValue > bValue) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
    
    this.filteredServers = result;
  }

  sortServers(column: string): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.filterServers();
  }

  toggleSelectAll(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.selectedServers = checked ? [...this.filteredServers] : [];
  }

  isSelected(server: ServerDto): boolean {
    return this.selectedServers.some(s => s.name === server.name);
  }

  selectServer(server: ServerDto, event: MouseEvent): void {
    if ((event.target as HTMLElement).tagName === 'INPUT') return;
    
    const index = this.selectedServers.findIndex(s => s.name === server.name);
    if (index === -1) {
      this.selectedServers.push(server);
    } else {
      this.selectedServers.splice(index, 1);
    }
  }

  toggleServerSelection(server: ServerDto): void {
    const index = this.selectedServers.findIndex(s => s.name === server.name);
    if (index === -1) {
      this.selectedServers.push(server);
    } else {
      this.selectedServers.splice(index, 1);
    }
  }

  get allSelected(): boolean {
    return this.filteredServers.length > 0 && 
           this.selectedServers.length === this.filteredServers.length;
  }

  // Gestion du dropdown
  toggleManageDropdown(): void {
    this.showManageDropdown = !this.showManageDropdown;
  }

  // Actions du dropdown
  showServerOverview(): void {
    console.log('Show server overview');
    this.showManageDropdown = false;
  }

 

  showServerStats(): void {
    console.log('Show server stats');
    this.showManageDropdown = false;
  }

   closeServer() {
    // logique pour mettre le serveur en maintenance
    this.alertMessage = "Cron jobs of this server are now disabled";
    this.showAlert = true;

    // cacher automatiquement après 3 secondes
    setTimeout(() => {
      this.showAlert = false;
    }, 3000);
  }
  restartServer(): void {
    console.log('Restart server');
    this.showManageDropdown = false;
  }

  refreshServer(): void {
    console.log('Refresh server');
    this.showManageDropdown = false;
  }

  deleteServer(): void {
    console.log('Delete server');
    this.showManageDropdown = false;
  }

  createServer(): void {
    console.log('Create new server');
  }

  showServerDetails(server: ServerDto): void {
    console.log('Show details for', server.name);
  }

  
}
