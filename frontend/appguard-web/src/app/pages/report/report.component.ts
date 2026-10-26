import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { ReportService } from '../../services/reportservice/report.service';
import { ReportDataDto } from '../../models/reportdata';
import jsPDF from 'jspdf';
import  html2canvasn from 'html2canvas';

interface FailedExecution {
  cronJobName: string;
  serverName: string;
  startTime: string; // ou Date
  exitCode: number;
  status: string;
  errorOutput: string;
}
@Component({
  selector: 'app-report',
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.css'],
  standalone: false
})
export class ReportComponent  {

 report: ReportDataDto | null = null;
  showReportOverlay = false; // contrôle l'affichage de l'overlay
  constructor(private reportService: ReportService) {}

 generateReport(): void {
    this.reportService.generateGlobalReport().subscribe({
      next: (data: ReportDataDto & { recentFailures: FailedExecution[] }) => {
        this.report = data;
        // Récupérer les failures depuis la réponse
        
        this.showReportOverlay = true; // afficher l'overlay
      },
      error: (err) => {
        console.error('Error generating report', err);
      }
    });
  }



 // Tableau indépendant pour les échecs récents
  recentFailures: FailedExecution[] = [
    { cronJobName: 'error testtt', serverName: 'UNRESOLVED', startTime: '2025-08-04 00:14:00', exitCode: 2, status: 'FAILURE', errorOutput: "Execution failed for CronJob 'error testtt' with exit code 2" },
    { cronJobName: 'error testtt', serverName: 'UNRESOLVED', startTime: '2025-08-04 00:16:00', exitCode: 2, status: 'FAILURE', errorOutput: "Execution failed for CronJob 'error testtt' with exit code 2" },
    { cronJobName: 'List Root Directory', serverName: 'UNRESOLVED', startTime: '2025-08-04 09:36:00', exitCode: -1, status: 'AGENT_UNREACHABLE', errorOutput: "Agent failed for CronJob 'List Root Directory' after 3 attempts: 401 on POST request for \"http://192.168.90.57:8000/execute\": [no body]" },
    { cronJobName: 'GestionRH', serverName: 'UNRESOLVED', startTime: '2025-08-29 06:59:19', exitCode: 2, status: 'FAILURE', errorOutput: "Execution failed for CronJob 'GestionRH' with exit code 2" }
  ];

  closeOverlay(): void {
    this.showReportOverlay = false;
  }



}

function html2canvas(reportElement: HTMLElement) {
  throw new Error('Function not implemented.');
}

