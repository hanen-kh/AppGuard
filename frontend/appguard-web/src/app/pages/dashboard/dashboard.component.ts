import { Component } from '@angular/core';
import { OnInit, OnDestroy,  HostListener } from '@angular/core';
import { DashboardData } from '../../models/Dashboard';
import { DashboardService } from '../../services/dashboardservice/dashboard.service';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';


interface Server {
  id: string;
  name: string;
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'UNREACHABLE';
  os: string;
  client: string;
  cronJobs: number;
  applications: number;
  services: number;
  selected?: boolean;
  hidden?: boolean;
  rollup?: boolean;
}

interface ServerData {
  servers: Server[];
  totalServers: number;
  activeServers: number;
  maintenanceServers: number;
  errorServers: number;
  inactiveServers: number;
}
@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})

export class DashboardComponent  implements OnInit {
 
 anchorSearch: string = '';

   data?: DashboardData;
  loading = true;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getDashboardOverview().subscribe({
      next: (res) => {
        this.data = res;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading dashboard:', err);
        this.loading = false;
      },
    });
  }


  scrollToSection() {
  const query = this.anchorSearch.toLowerCase();
  let id = '';

  if (query.includes('server')) id = 'servers-section';
  else if (query.includes('cron')) id = 'cron-section';
  else if (query.includes('alert')) id = 'alerts-section';

  const element = document.getElementById(id);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' });
  }
}
}