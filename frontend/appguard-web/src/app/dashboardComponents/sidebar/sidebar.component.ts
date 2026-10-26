import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
import { RouterLinkActive } from '@angular/router';


interface MenuItem {
  icon: string;
  label: string;
  route: string;
  active?: boolean;
}
@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent { user = {
  name: 'Hanen Khmilet',
  email: 'khmilet@gmail.com',
  profilePic: '/assets/images/AppGard.png'
};

menuItems: MenuItem[] = [
  { icon: 'fa-th-large', label: 'Your Dashboard', route: '/dashboard', active: true },
    { icon: 'fa-file', label: 'Metrics', route: '/metrics' },

  { icon: 'fa-user-plus', label: 'Add New User', route: '/adduser' },
  { icon: 'fa-users', label: 'View All Users', route: '/users' },
  { icon: 'fa-server', label: 'Servers', route: '/servers' },
   { icon: 'fa-headset', label: 'Server Monitor', route: '/serveroverview' },
    { icon: 'fa-clock-rotate-left', label: 'Cron Jobs', route: '/cronjobs' }, 
      { icon: 'fa-history', label: 'Cron History', route: '/cronhistory' }, // <-- nouvel item
  { icon: 'fa-user-tie', label: 'Clients', route: '/clientslist' }, 
  { icon: 'fa-chart-bar', label: 'Reports', route: '/report' }
 

  
];

setActive(item: MenuItem): void {
  this.menuItems.forEach(menuItem => menuItem.active = false);
  item.active = true;
}

logout(): void {
  // Logique de déconnexion
  console.log('Déconnexion...');
}
}