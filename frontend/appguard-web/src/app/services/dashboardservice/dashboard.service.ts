import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DashboardData } from '../../models/Dashboard';
import { HttpClient } from '@angular/common/http';
@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private baseUrl = 'http://localhost:8081/api/dashboard';
  
  constructor(private http: HttpClient) { }

   getDashboardOverview(): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.baseUrl}/overview`);
  }
}
