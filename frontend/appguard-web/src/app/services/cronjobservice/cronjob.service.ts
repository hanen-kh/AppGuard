import { Injectable } from '@angular/core';
import { CronJob } from '../../models/cronjob';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class CronjobService {
private apiUrl = 'http://localhost:8081/api/cron-jobs'; 
  constructor(private http: HttpClient) {}

  // Récupérer tous les cron jobs
  getAllCronJobs(): Observable<CronJob[]> {
   return this.http.get<CronJob[]>(`${this.apiUrl}/allcronjobs`);
  }

  // Récupérer un cron job par ID
  getCronJobById(id: number): Observable<CronJob> {
    return this.http.get<CronJob>(`${this.apiUrl}/${id}`);
  }

  // Créer un cron job pour un serveur donné
 createCronJob(serverId: number, cronJob: any): Observable<void> {
  return this.http.post<void>(`${this.apiUrl}/server/${serverId}`, cronJob);
}

  // Mettre à jour un cron job
  updateCronJob(id: number, cronJob: Partial<CronJob>): Observable<CronJob> {
    return this.http.put<CronJob>(`${this.apiUrl}/${id}`, cronJob);
  }

  // Supprimer un cron job
  deleteCronJob(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
