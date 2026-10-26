import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';import {  Client } from '../../models/ClientDTO';
import { Supervisor } from '../../models/supervisor';
@Injectable({
  providedIn: 'root'
})
export class ClientService {
 private apiUrl = 'http://localhost:8081/api/clients';

  constructor(private http: HttpClient) { }

 getClientsOfCurrentUser(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.apiUrl}/by_user`);
  }

  createClient(clientDto: Client): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/create`, clientDto);
  }

  updateClient(id: number, clientDto: Client): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/update/${id}`, clientDto);
  }

  deleteClient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }

  getClientById(id: number): Observable<Client> {
    return this.http.get<Client>(`${this.apiUrl}/getBy_id/${id}`);
  }

  getSupervisorCount(client: Client): number {
    return client.supervisorIds ? client.supervisorIds.length : 0;
  }

  // client.service.ts
getAllSupervisors(): Observable<Supervisor[]> {
  return this.http.get<Supervisor[]>('http://localhost:8081/api/users/supervisors');
}

}