import { Injectable } from '@angular/core';
import { Application } from '../../models/application';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class ApplicationService {

 private baseUrl = 'http://localhost:8081/api/applications'; // Remplace par ton URL backend

  constructor(private http: HttpClient) {}

  getAllApplications(): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.baseUrl}/all_apps`);
  }

  getApplicationByName(name: string): Observable<Application> {
  return this.http.get<Application>(`${this.baseUrl}/by_name/${name}`);
}

}
