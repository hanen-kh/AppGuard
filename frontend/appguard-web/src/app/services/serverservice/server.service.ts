import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ServerDto } from '../../models/Server';
import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class ServerService {

    private baseUrl = 'http://localhost:8081/api/servers'; 

  constructor(private http: HttpClient) {}

  getAllServers(): Observable<ServerDto[]> {
    return this.http.get<ServerDto[]>(`${this.baseUrl}/all`);
  }

   enableMaintenanceMode(serverId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${serverId}/maintenance/enable`, {});
  }

  disableMaintenanceMode(serverId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${serverId}/maintenance/disable`, {});
  }
}
