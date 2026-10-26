import { Component, OnInit, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { ServicestatusService } from '../../services/metricservice/servicestatus.service';
import { ChartConfiguration, ChartOptions, registerables } from 'chart.js';

import {
  Chart,
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  TimeScale,
  Title,
  Tooltip,
  Legend,
  CategoryScale
} from 'chart.js';
import { HttpClient } from '@angular/common/http';
import { ServiceStatus } from '../../models/ServiceStatus';
import { Subscription } from 'rxjs';

Chart.register(LineController, LineElement, PointElement, LinearScale, TimeScale, Title, Tooltip, Legend, CategoryScale);

@Component({
  selector: 'app-metrics',
  standalone: false,
  templateUrl: './metrics.component.html',
  styleUrl: './metrics.component.css'
})
export class MetricsComponent implements OnInit, AfterViewInit, OnDestroy {
  serviceName: string = 'mysql';
  filteredStatuses: ServiceStatus[] = [];

  // Seuils configurables
  cpuThreshold: number = 80;
  memoryThreshold: number = 75;
  diskThreshold: number = 90;

  // Message d'alerte flottant
  alertMessage: string | null = null;
  private alertTimeout: any;

  @ViewChild('cpuMemoryChart') cpuMemoryChartRef!: ElementRef;
  @ViewChild('diskChart') diskChartRef!: ElementRef;
  
  cpuMemoryChart: any;
  diskChart: any;

  private statusSubscription?: Subscription;

  constructor(private serviceStatusService: ServicestatusService) {}

  ngOnInit(): void {
    this.statusSubscription = this.serviceStatusService.status$.subscribe(status => {
      if (status) {
        this.filteredStatuses.push(status);
        if (this.filteredStatuses.length > 50) this.filteredStatuses.shift();

        this.updateCharts(status);
        this.checkThresholds(status); // 🔹 Vérification des seuils
      }
    });
  }

  // Vérification des seuils
  checkThresholds(status: ServiceStatus): void {
    const cpu = status.cpuUsage * 100;
    const mem = status.memoryUsage * 100;
    const disk = status.diskUsage * 100;

    if (cpu > this.cpuThreshold) {
      this.showAlert(`⚠️ Critical: CPU usage exceeded threshold (${cpu.toFixed(1)}% > ${this.cpuThreshold}%)`);
    }
    if (mem > this.memoryThreshold) {
      this.showAlert(`⚠️ Critical: Memory usage exceeded threshold (${mem.toFixed(1)}% > ${this.memoryThreshold}%)`);
    }
    if (disk > this.diskThreshold) {
      this.showAlert(`⚠️ Critical: Disk usage exceeded threshold (${disk.toFixed(1)}% > ${this.diskThreshold}%)`);
    }
  }

  // Afficher une alerte flottante (toast)
  showAlert(message: string): void {
    this.alertMessage = message;

    if (this.alertTimeout) clearTimeout(this.alertTimeout);
    this.alertTimeout = setTimeout(() => {
      this.alertMessage = null;
    }, 5000); // disparaît après 5s
  }


 



  ngAfterViewInit(): void {
    this.filteredStatuses = this.initEmptyData();
    this.createCharts();
  }

  ngOnDestroy(): void {
    // 🔹 Désabonnement pour libérer le WebSocket
    this.statusSubscription?.unsubscribe();

    // 🔹 Optionnel : fermer explicitement le WebSocket si ton service expose une méthode
    if (this.serviceStatusService.closeWebSocket) {
      this.serviceStatusService.closeWebSocket();
    }

    if (this.cpuMemoryChart) {
      this.cpuMemoryChart.destroy();
    }
    if (this.diskChart) {
      this.diskChart.destroy();
    }
  }

  createCharts(): void {
    const labels = this.filteredStatuses.map(s => new Date(s.lastChecked).toLocaleTimeString());
    const cpuData = this.filteredStatuses.map(s => s.cpuUsage * 100);
    const memoryData = this.filteredStatuses.map(s => s.memoryUsage * 100);
    const diskData = this.filteredStatuses.map(s => s.diskUsage * 100);

    // Détruire les graphiques existants
    if (this.cpuMemoryChart) this.cpuMemoryChart.destroy();
    if (this.diskChart) this.diskChart.destroy();

    // Créer le graphique CPU/Mémoire
    this.cpuMemoryChart = new Chart(this.cpuMemoryChartRef.nativeElement, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          { 
            label: 'CPU (%)', 
            data: cpuData, 
            borderColor: 'rgba(59, 130, 246, 1)', // Bleu
            backgroundColor: 'rgba(59, 130, 246, 0.1)',
            fill: true,
            tension: 0.4,
            borderWidth: 3,
            pointBackgroundColor: 'rgba(59, 130, 246, 1)',
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            pointRadius: 4
          },
          { 
            label: 'Mémoire (%)', 
            data: memoryData, 
            borderColor: 'rgba(16, 185, 129, 1)', // Vert
            backgroundColor: 'rgba(16, 185, 129, 0.1)',
            fill: true,
            tension: 0.4,
            borderWidth: 3,
            pointBackgroundColor: 'rgba(16, 185, 129, 1)',
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            pointRadius: 4
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: {
          duration: 750,
          easing: 'easeInOutQuart'
        },
        interaction: {
          intersect: false,
          mode: 'index'
        },
        plugins: { 
          title: { 
            display: true, 
            text: 'CPU et Mémoire',
            font: {
              size: 18,
              weight: 'bold'
            },
            color: '#374151'
          },
          legend: {
            display: true,
            position: 'top',
            labels: {
              usePointStyle: true,
              font: {
                size: 12,
                weight: 'normal'
              }
            }
          },
          tooltip: {
            backgroundColor: 'rgba(0, 0, 0, 0.8)',
            titleColor: '#fff',
            bodyColor: '#fff',
            borderColor: 'rgba(255, 255, 255, 0.1)',
            borderWidth: 1,
            cornerRadius: 8,
            displayColors: true
          }
        },
        scales: {
          y: { 
            beginAtZero: true, 
            max: 3,
            title: { 
              display: true, 
              text: 'Utilisation (%)',
              font: {
                size: 14,
                weight: 'normal'
              }
            },
            grid: {
              color: 'rgba(0, 0, 0, 0.1)'
            },
            ticks: {
              callback: function(value) {
                return value + '%';
              }
            }
          },
          x: { 
            title: { 
              display: true, 
              text: 'Temps',
              font: {
                size: 14,
                weight: 'normal'
              }
            },
            grid: {
              color: 'rgba(0, 0, 0, 0.1)'
            }
          }
        }
      }
    });

    // Créer le graphique Disque
    this.diskChart = new Chart(this.diskChartRef.nativeElement, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          { 
            label: 'Disque (%)', 
            data: diskData, 
            borderColor: 'rgba(245, 101, 101, 1)', // Rouge
            backgroundColor: 'rgba(245, 101, 101, 0.1)',
            fill: true,
            tension: 0.4,
            borderWidth: 3,
            pointBackgroundColor: 'rgba(245, 101, 101, 1)',
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            pointRadius: 4
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: {
          duration: 750,
          easing: 'easeInOutQuart'
        },
        interaction: {
          intersect: false,
          mode: 'index'
        },
        plugins: { 
          title: { 
            display: true, 
            text: 'Utilisation du Disque',
            font: {
              size: 18,
              weight: 'bold'
            },
            color: '#374151'
          },
          legend: {
            display: true,
            position: 'top',
            labels: {
              usePointStyle: true,
              font: {
                size: 12,
                weight: 'normal'
              }
            }
          },
          tooltip: {
            backgroundColor: 'rgba(0, 0, 0, 0.8)',
            titleColor: '#fff',
            bodyColor: '#fff',
            borderColor: 'rgba(255, 255, 255, 0.1)',
            borderWidth: 1,
            cornerRadius: 8,
            displayColors: true
          }
        },
        scales: {
          y: { 
            beginAtZero: true, 
            max: 7000,
            title: { 
              display: true, 
              text: 'Utilisation (%)',
              font: {
                size: 14,
                weight: 'normal'
              }
            },
            grid: {
              color: 'rgba(0, 0, 0, 0.1)'
            },
            ticks: {
              callback: function(value) {
                return value + '%';
              }
            }
          },
          x: { 
            title: { 
              display: true, 
              text: 'Temps',
              font: {
                size: 14,
                weight: 'normal'
              }
            },
            grid: {
              color: 'rgba(0, 0, 0, 0.1)'
            }
          }
        }
      }
    });
  }

  updateCharts(newStatus: ServiceStatus): void {
    const time = new Date(newStatus.lastChecked).toLocaleTimeString();

    // Mise à jour du graphique CPU/Mémoire
    if (this.cpuMemoryChart?.data) {
      this.cpuMemoryChart.data.labels.push(time);
      this.cpuMemoryChart.data.datasets[0].data.push(newStatus.cpuUsage * 100);
      this.cpuMemoryChart.data.datasets[1].data.push(newStatus.memoryUsage * 100);

      if (this.cpuMemoryChart.data.labels.length > 50) {
        this.cpuMemoryChart.data.labels.shift();
        this.cpuMemoryChart.data.datasets.forEach((dataset: { data: any[] }) => dataset.data.shift());
      }

      this.cpuMemoryChart.update('none'); // 'none' pour une mise à jour sans animation
    }

    // Mise à jour du graphique Disque
    if (this.diskChart?.data) {
      this.diskChart.data.labels.push(time);
      this.diskChart.data.datasets[0].data.push(newStatus.diskUsage * 100);

      if (this.diskChart.data.labels.length > 50) {
        this.diskChart.data.labels.shift();
        this.diskChart.data.datasets[0].data.shift();
      }

      this.diskChart.update('none'); // 'none' pour une mise à jour sans animation
    }
  }

  initEmptyData(): ServiceStatus[] {
    const empty: ServiceStatus[] = [];
    const now = new Date();

    for (let i = 0; i < 20; i++) {
      empty.push({
        serviceName: this.serviceName,
        cpuUsage: Math.random() * 0.8, // Données de test
        memoryUsage: Math.random() * 0.6,
        diskUsage: Math.random() * 0.4,
        lastChecked: new Date(now.getTime() - (20 - i) * 1000).toISOString(),
        id: null,
        port: 0,
        isActive: true
      });
    }

    return empty;
  }

  // Méthode trackBy pour optimiser le rendu du tableau
  trackByFn(index: number, item: ServiceStatus): any {
    return item.lastChecked || index;
  }
}