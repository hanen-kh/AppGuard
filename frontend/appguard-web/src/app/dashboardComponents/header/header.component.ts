import { Component, OnInit } from '@angular/core';
import { Input } from '@angular/core';
import { AlertsService } from '../../services/alerts/alerts.service';
import { Router } from '@angular/router';
import { NewEntityAlert } from '../../models/NewEntityAlert';
@Component({
  selector: 'app-header',
  standalone: false,
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  showNotifications: boolean = false;
  alerts: { message: string, time: string, type: string }[] = [];
  unreadCount: number = 0;

  constructor(private alertService: AlertsService, private router: Router) {
    this.alertService.getAlerts().subscribe((alert: NewEntityAlert) => {
      const displayAlert = {
        message: alert.message,
        time: new Date().toLocaleTimeString(),
        type: this.determineDisplayType(alert.type) // Mapper type backend vers type affichage
      };
      this.alerts.unshift(displayAlert);
      this.unreadCount++;
    });
  }

  ngOnInit(): void {}

  private determineDisplayType(type: string): string {
    switch (type.toUpperCase()) {
      case 'SERVER':
        return 'info'; // Mapper "SERVER" à "info" pour l'affichage
      case 'APPLICATION':
        return 'success'; // Mapper "APPLICATION" à "success"
      default:
        return 'info'; // Par défaut
    }
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      this.unreadCount = 0;
    }
  }

  markAllAsRead(): void {
    this.unreadCount = 0;
  }

  navigateToMetrics(): void {
    this.router.navigate(['/metrics']);
    this.toggleNotifications();
  }
}