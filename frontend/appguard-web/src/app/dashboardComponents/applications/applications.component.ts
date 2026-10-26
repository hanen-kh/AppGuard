import { Component } from '@angular/core';

import { OnInit, OnDestroy } from '@angular/core';
import { Application } from '../../models/application';
import { ApplicationService } from '../../services/applicationservice/application.service';
import { Router } from '@angular/router';
@Component({
  selector: 'app-applications',
  standalone: false,
  templateUrl: './applications.component.html',
  styleUrl: './applications.component.css'
})
export class ApplicationsComponent implements OnInit{



    applications: Application[] = [];
 

  constructor(private appService: ApplicationService,  private router: Router) {}

  ngOnInit(): void {
    this.appService.getAllApplications().subscribe({
      next: (data) => this.applications = data,
      error: (err) => console.error('Erreur lors du chargement des applications', err)
    });
  }

  selectApp(app: Application): void {
    this.router.navigate(['/applicationDetails', app.name]);
  }
}
