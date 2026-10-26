import { Component, OnInit } from '@angular/core';
import { Chart } from 'chart.js';

@Component({
  selector: 'app-serveroverwiew',
  standalone: false,
  templateUrl: './serveroverwiew.component.html',
  styleUrl: './serveroverwiew.component.css'
})
export class ServeroverwiewComponent implements OnInit {

  ngOnInit(): void {
    this.createCpuChart();
    this.createMemoryChart();
    this.createDiskChart();
  }

  createCpuChart(): void {
    const ctx = document.getElementById('cpuChart') as HTMLCanvasElement;
    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['00:00', '01:00', '02:00', '03:00', '04:00', '05:00', '06:00'],
        datasets: [{
          label: 'CPU Usage (%)',
          data: [45, 60, 50, 70, 55, 65, 50], // données fictives
          borderColor: 'rgba(75, 192, 192, 1)',
          backgroundColor: 'rgba(75, 192, 192, 0.2)',
          tension: 0.3
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { display: true }
        },
        scales: {
          y: { beginAtZero: true, max: 100 }
        }
      }
    });
  }

  createMemoryChart(): void {
    const ctx = document.getElementById('memoryChart') as HTMLCanvasElement;
    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['00:00', '01:00', '02:00', '03:00', '04:00', '05:00', '06:00'],
        datasets: [{
          label: 'Memory Usage (%)',
          data: [30, 40, 35, 50, 45, 55, 50],
          borderColor: 'rgba(255, 99, 132, 1)',
          backgroundColor: 'rgba(255, 99, 132, 0.2)',
          tension: 0.3
        }]
      },
      options: {
        responsive: true,
        scales: { y: { beginAtZero: true, max: 100 } }
      }
    });
  }

  createDiskChart(): void {
    const ctx = document.getElementById('diskChart') as HTMLCanvasElement;
    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['00:00', '01:00', '02:00', '03:00', '04:00', '05:00', '06:00'],
        datasets: [{
          label: 'Disk Usage (%)',
          data: [20, 25, 22, 35, 30, 40, 38],
          borderColor: 'rgba(54, 162, 235, 1)',
          backgroundColor: 'rgba(54, 162, 235, 0.2)',
          tension: 0.3
        }]
      },
      options: {
        responsive: true,
        scales: { y: { beginAtZero: true, max: 100 } }
      }
    });
  }

}