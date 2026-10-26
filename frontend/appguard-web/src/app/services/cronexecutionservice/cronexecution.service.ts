import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CronExecution } from '../../models/cronExecution';
@Injectable({
  providedIn: 'root'
})
export class CronexecutionService {

   private baseUrl = 'http://localhost:8081/api/job-executions/executions'; // adapte selon ton backend

  constructor(private http: HttpClient) {}

  getAllExecutions(): Observable<CronExecution[]> {
    return this.http.get<CronExecution[]>(this.baseUrl);
  }
}