import { Injectable } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Observable, Subject } from 'rxjs';
import { ReportDataDto } from '../../models/reportdata';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
   constructor(private http: HttpClient) {}

  /** Déclenche la génération du rapport côté backend et retourne le résultat */
  generateGlobalReport(): Observable<ReportDataDto> {
    return this.http.post<ReportDataDto>('http://localhost:8081/api/reports/generate', {});
  }
}
