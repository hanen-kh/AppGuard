import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Application } from '../../../models/application';
import { ApplicationService } from '../../../services/applicationservice/application.service';
@Component({
  selector: 'app-application-details',
  standalone: false,
  templateUrl: './application-details.component.html',
  styleUrl: './application-details.component.css'
})
export class ApplicationDetailsComponent implements OnInit{

  selectedApp: Application | null = null;

  constructor(
    private route: ActivatedRoute,
    private appService: ApplicationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const appName = this.route.snapshot.paramMap.get('appName');
    if (appName) {
      this.appService.getApplicationByName(appName).subscribe(data => {
        this.selectedApp = data;
      });
    }
  }


  navigateToMetrics() {
    // Navigue vers la page des métriques avec le nom du service comme paramètre
    this.router.navigate(['/metrics']);
  }

}
