import { Component } from '@angular/core';
import { OnInit, AfterViewInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Validators } from '@angular/forms';
import { FormGroup } from '@angular/forms';
import { UserService} from '../../../services/userservice/user.service';
import { User } from '../../../models/User';
import Chart from 'chart.js/auto';
@Component({
  selector: 'app-adduser',
  standalone: false,
  templateUrl: './adduser.component.html',
  styleUrl: './adduser.component.css'
})
export class AdduserComponent implements OnInit,  AfterViewInit{

  userForm!: FormGroup;
  
  errorMessage: string = '';
  roles = [
    { id: 1, name: 'ADMIN' },
    { id: 2, name: 'DATAANALYST' },
    { id: 3, name: 'SUPERVISOR' }
  ];

  users: User[] = [];
  selectedUser: User | null = null;
  isEditMode = false;
  
  totalUsers: number = 0;
  adminCount: number = 0;
  supervisorCount: number = 0;
  dataAnalystCount: number = 0;
  usersChart: any;

  selectedRole: string = ''; // Rôle sélectionné
filteredUsers: User[] = []; // Liste filtrée des utilisateurs

  constructor(private userService: UserService, private fb: FormBuilder) {
    // Initialisation du formulaire
    this.userForm = this.fb.group({
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      roleId: ['', Validators.required] // ← rôle basé sur l'ID maintenant
    });
    
  }

  ngOnInit(): void {
    this.userForm = this.fb.group({
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      
      roleId: ['', Validators.required] 
    });

    // Appeler loadUsers() pour charger les statistiques dès l'initialisation
  this.loadUsers();
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

  // Envoi de la mise à jour au backend
  onSubmit(): void {
    if (this.userForm.valid) {
      const newUser: User = this.userForm.value;

      this.errorMessage = ''; // Réinitialiser le message d'erreur

      this.userService.createUser(newUser).subscribe(
        () => {
          console.log('User created successfully!');
          this.userForm.reset(); // Réinitialiser le formulaire
        },
        (error: string) => {
          console.error('Error creating user:', error);
          this.errorMessage = error; // Afficher l'erreur du backend
        }
      );
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
  
}

