import { Component, OnInit } from '@angular/core';
import { ClientService } from '../../../services/clientservice/client.service';
import {  Client } from '../../../models/ClientDTO';
@Component({
  selector: 'app-clientslist',
  standalone: false,
  templateUrl: './clientslist.component.html',
  styleUrl: './clientslist.component.css'
})
export class ClientslistComponent implements OnInit {
  clients: Client[] = [];
  filteredClients: Client[] = [];
  isLoading = true;
  searchQuery = '';
  currentUser = { role: 'ADMIN', email: 'admin@appguard.com' };

  // Statistics
  totalClients = 0;
  totalSupervisors = 0;
  activeClients = 0;

  constructor(private clientService: ClientService) { }

  ngOnInit(): void {
    this.loadClients();
    this.updateUserInterface();
  }

 loadClients(): void {
  this.isLoading = true;

  this.clientService.getClientsOfCurrentUser().subscribe({
    next: (clientsData) => {
      // On conserve la valeur du backend ou on calcule si absente
      this.clients = clientsData.map(clientData => ({
        ...clientData,
        supervisorCount: clientData.supervisorCount ?? clientData.supervisorIds.length
      }));
      
      this.filteredClients = [...this.clients];
      this.updateStatistics();
      this.isLoading = false;
    },
    error: (error) => {
      console.error('Error loading clients:', error);
      this.isLoading = false;
    }
  });
}


  updateStatistics(): void {
    this.totalClients = this.clients.length;

    // Calculer les superviseurs uniques
    const allSupervisorIds = new Set<number>();
    this.clients.forEach(client => {
      client.supervisorIds.forEach(id => allSupervisorIds.add(id));
    });
    this.totalSupervisors = allSupervisorIds.size;

    // Pour démo, on suppose 85% de clients actifs
    this.activeClients = Math.floor(this.totalClients * 0.85);
  }

  updateUserInterface(): void {
    // Implémenter la logique pour récupérer le rôle réel de l'utilisateur si nécessaire
  }

  handleSearch(): void {
    const query = this.searchQuery.toLowerCase().trim();

    if (!query) {
      this.filteredClients = [...this.clients];
    } else {
      this.filteredClients = this.clients.filter(client =>
        client.name.toLowerCase().includes(query) ||
        client.contactEmail.toLowerCase().includes(query) ||
        client.description.toLowerCase().includes(query)
      );
    }
  }

  viewClient(client: Client): void {
    alert(`Client Details:\n\nName: ${client.name}\nEmail: ${client.contactEmail}\nDescription: ${client.description}\nSupervisors: ${client.supervisorCount}`);
  }

  editClient(client: Client): void {
    alert(`Edit client: ${client.name}`);
    // Naviguer ou ouvrir un modal dans une vraie application
  }

  deleteClient(client: Client): void {
    if (confirm(`Are you sure you want to delete the client "${client.name}"?`)) {
      if (client.id) {
        this.clientService.deleteClient(client.id).subscribe({
          next: () => {
            this.clients = this.clients.filter(c => c.id !== client.id);
            this.filteredClients = this.filteredClients.filter(c => c.id !== client.id);
            this.updateStatistics();
          },
          error: (error) => {
            console.error('Error deleting client:', error);
            alert('Error deleting client. Please try again.');
          }
        });
      }
    }
  }

 showAddClientForm = false;

addClient(): void {
  this.showAddClientForm = true; // Affiche le formulaire
}

closeAddClientForm(): void {
  this.showAddClientForm = false; // Ferme le formulaire
}

  exportClients(): void {
    alert(`Exporting ${this.filteredClients.length} clients...`);
  }

// Méthode appelée quand AddclientComponent émet un client ajouté
onClientAdded(client: Client): void {
  this.clients.push(client);
  this.filteredClients = [...this.clients];
  this.updateStatistics();
  this.closeAddClientForm();
}
  
}