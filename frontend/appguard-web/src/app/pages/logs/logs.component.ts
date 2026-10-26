import { Component, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { OnInit, OnDestroy } from '@angular/core';
import { ServiceStatus } from '../../models/ServiceStatus';
import { ServicestatusService } from '../../services/metricservice/servicestatus.service';
import { LogService } from '../../services/logservice/log.service';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { Chart } from 'chart.js';
import { LogEntry } from '../../models/LogEntry';

// 🔹 Déclare le type en dehors du composant
type LogLevel = 'INFO' | 'WARN' | 'ERROR' | 'DEBUG';

@Component({
  selector: 'app-logs',
  standalone: false,
  templateUrl: './logs.component.html',
  styleUrl: './logs.component.css'
})
export class LogsComponent implements OnInit, AfterViewInit, OnDestroy {

  filterLevel: string = '';
  filterStartDate: string = '';
  filterEndDate: string = '';

  @ViewChild('logChart') chartRef!: ElementRef;
  
  levelStats: Record<LogLevel, number> = {
    INFO: 0,
    WARN: 0,
    ERROR: 0,
    DEBUG: 0
  };

  logs: LogEntry[] = [];
  private logSubscription?: Subscription;
  private appName: string = 'appguard'; // 🔹 Nom fixe
  logChart: any;

  constructor(private logWebSocketService: LogService) {}

  ngOnInit(): void {
    // Réinitialisation
    if (this.logSubscription) {
      this.logSubscription.unsubscribe();
    }
    this.logs = [];
    this.resetStats();

    // 🔹 1. Historique pour appguard
    this.logWebSocketService.getPreviousLogs(this.appName).subscribe(logs => {
      this.logs = logs.reverse();
      this.calculateStats();
      this.initChart();
    });

    // 🔹 2. Temps réel (global)
    this.logSubscription = this.logWebSocketService.getLogs().subscribe(log => {
      this.logs.unshift(log);
      this.updateStats(log.level);
      this.updateChart();
    });
  }

  ngAfterViewInit(): void {
    if (this.logs.length > 0) {
      this.initChart(); // ⚠️ canvas doit exister
    }
  }

  private resetStats(): void {
    this.levelStats = { INFO: 0, WARN: 0, ERROR: 0, DEBUG: 0 };
  }

  private calculateStats(): void {
    this.resetStats();
    this.logs.forEach(log => {
      if (log.level && ['INFO', 'WARN', 'ERROR', 'DEBUG'].includes(log.level)) {
        this.levelStats[log.level as LogLevel]++;
      }
    });
  }

  private updateStats(level: string): void {
    if (['INFO', 'WARN', 'ERROR', 'DEBUG'].includes(level)) {
      this.levelStats[level as LogLevel]++;
    }
  }

  private initChart(): void {
    if (this.chartRef) {
      const ctx = this.chartRef.nativeElement.getContext('2d');
      
      if (this.logChart) {
        this.logChart.destroy();
      }

      this.logChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: Object.keys(this.levelStats),
          datasets: [{
            data: Object.values(this.levelStats),
            backgroundColor: [
              '#2196F3', // INFO - blue
              '#FF9800', // WARN - orange
              '#F44336', // ERROR - red
              '#9C27B0'  // DEBUG - purple
            ],
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { position: 'right' },
            tooltip: {
              callbacks: {
                label: (context) => {
                  const label = context.label || '';
                  const value = context.raw as number || 0;
                  const total = context.dataset.data.reduce((a: number, b: number) => a + b, 0);
                  const percentage = Math.round((value / total) * 100);
                  return `${label}: ${value} (${percentage}%)`;
                }
              }
            }
          }
        }
      });
    }
  }

  private updateChart(): void {
    if (this.logChart) {
      this.logChart.data.datasets[0].data = Object.values(this.levelStats);
      this.logChart.update();
    }
  }

  onFilter(): void {
  // Préparer les dates en ISO format
  let startDateIso = this.filterStartDate ? this.filterStartDate + 'T00:00:00Z' : null;
  let endDateIso = this.filterEndDate ? this.filterEndDate + 'T23:59:59Z' : null;

  // 🔹 Appel au service sans passer appName (fixé à "appguard" dans le service)
  this.logWebSocketService.getLogsFiltered(
    this.filterLevel || null,
    startDateIso,
    endDateIso
  ).subscribe(logs => {
    this.logs = logs.reverse();
    this.calculateStats();
    this.updateChart();
  });
}



  ngOnDestroy(): void {
    this.logSubscription?.unsubscribe();
  }

  expandedRows: Set<number> = new Set();

  getStatusClass(level: string): string {
    switch(level) {
      case 'INFO': return 'status-info';
      case 'WARN': return 'status-warning';
      case 'ERROR': return 'status-error';
      case 'DEBUG': return 'status-success';
      default: return 'status-info';
    }
  }

  toggleExpand(index: number): void {
    if (this.expandedRows.has(index)) {
      this.expandedRows.delete(index);
    } else {
      this.expandedRows.add(index);
    }
  }

  isExpanded(index: number): boolean {
    return this.expandedRows.has(index);
  }

  truncateMessage(message: string, maxLength = 80): string {
    return message.length > maxLength ? message.substring(0, maxLength) + '...' : message;
  }
}
