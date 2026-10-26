import { Component } from '@angular/core';
import { OnInit, AfterViewInit } from '@angular/core';
import { User } from '../../../models/User';
import { UserService } from '../../../services/userservice/user.service';
import {  Validators } from '@angular/forms';
import { FormGroup } from '@angular/forms';
import { FormBuilder } from '@angular/forms';
import Chart from 'chart.js/auto';
@Component({
  selector: 'app-viewusers',
  standalone: false,
  templateUrl: './viewusers.component.html',
  styleUrl: './viewusers.component.css'
})
export class ViewusersComponent implements OnInit,  AfterViewInit{
  users: User[] = [];
  selectedUser: User | null = null;
  isEditMode = false;
  userForm: FormGroup;
  selectedRole: string = ''; // Rôle sélectionné
filteredUsers: User[] = []; // Liste filtrée des utilisateurs
totalUsers: number = 0;
  adminCount: number = 0;
  supervisorCount: number = 0;
  dataAnalystCount: number = 0;
  usersChart: any;
roles = ['ADMIN', 'SUPERVISOR','DATA_ANALYST'];
  constructor(private userService: UserService, private fb: FormBuilder) {
 // Initialisation du formulaire
 this.userForm = this.fb.group({
  firstname: ['', Validators.required],
  lastname: ['', Validators.required],
  email: ['', [Validators.required, Validators.email]],
  typeRole: ['', Validators.required]
});
}

ngOnInit(): void {
  // Récupération des utilisateurs depuis l'API
  this.userService.getAllUsers().subscribe({
    next: (data) => {
      this.users = data;
      this.filteredUsers = [...this.users]; // Assure l'affichage initial de tous les utilisateurs
    },
    error: (error) => {
      console.error('Erreur lors de la récupération des utilisateurs', error);
    }
  });
   // Appeler loadUsers() pour charger les statistiques dès l'initialisation
  this.loadUsers();
}


 




// Mode d'édition : remplit le formulaire avec les données de l'utilisateur sélectionné
editUser(user: User) {
  this.selectedUser = user;
  this.isEditMode = true;
  this.userForm.patchValue({
    firstname: user.firstname,
    lastname: user.lastname,
    email: user.email,
    typeRole: user.typeRole
  });
}

// Envoi de la mise à jour au backend
Update() {
  if (this.userForm.valid) {
    const updatedUser = this.userForm.value;
    this.userService.updateUser(this.selectedUser!.id, updatedUser).subscribe(
      (response) => {
        console.log('Utilisateur mis à jour avec succès');
        this.isEditMode = false;
        this.selectedUser = null;
          // Récupérer les utilisateurs et mettre à jour la liste locale
          this.userService.getAllUsers().subscribe((users) => {
            this.users = users; // <-- mise à jour directe du tableau
            this.filterUsersByRole();
          });

      },
      (error) => {
        console.error('Erreur lors de la mise à jour de l\'utilisateur', error);
      }
    );
  }
}

// Suppression d'un utilisateur
deleteUser(id: number) {
  if (confirm('Voulez-vous vraiment supprimer cet utilisateur ?')) {
    this.userService.deleteUser(id).subscribe(() => {
      this.users = this.users.filter(user => user.id !== id);
      this.filterUsersByRole(); 
      console.log('Utilisateur supprimé avec succès');
    });
  }
}
filterUsersByRole() {
  if (!this.selectedRole) {
    this.filteredUsers = this.users;
  } else {
    this.filteredUsers = this.users.filter(user => user.typeRole === this.selectedRole);
  }
}

loadUsers(): void {
  this.userService.getAllUsers().subscribe(
    (data: User[]) => {
      this.users = data;
      this.totalUsers = this.users.length;
      
      // Compter les utilisateurs par rôle
      this.adminCount = this.users.filter(user => user.typeRole === 'ADMIN').length;
      this.supervisorCount = this.users.filter(user => user.typeRole === 'SUPERVISOR').length;
      this.dataAnalystCount = this.users.filter(user => user.typeRole === 'DATA_ANALYST').length;
    },
    error => {
      console.error('Erreur lors du chargement des utilisateurs', error);
    }
  );
}

ngAfterViewInit(): void {
    // Initialiser le graphique après le chargement des données
    setTimeout(() => this.initializeChart(), 500);
  }

  initializeChart(): void {
    const ctx = document.getElementById('usersChart') as HTMLCanvasElement;
    
    if (this.usersChart) {
      this.usersChart.destroy();
    }
    
    this.usersChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Administrateurs', 'Superviseurs', 'Analystes de données'],
        datasets: [{
          data: [this.adminCount, this.supervisorCount, this.dataAnalystCount],
          backgroundColor: [
            '#4e73df',  // Bleu pour admin
            '#1cc88a',  // Vert pour superviseur
            '#f6c23e'   // Jaune pour analyste
          ],
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          title: {
            display: true,
            text: 'Répartition des utilisateurs par rôle'
          }
        }
      }
    });
  }

 

}



