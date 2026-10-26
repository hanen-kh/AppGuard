import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Client, IMessage, Stomp } from '@stomp/stompjs';
import * as StompJs from '@stomp/stompjs'; 
import SockJS from 'sockjs-client';
import { LogEntry } from '../../models/LogEntry';


@Injectable({
  providedIn: 'root'
})
export class LogService {
  private client: Client;
  private connected = false;

  private logsSubject = new Subject<any>();

  constructor(private http: HttpClient) {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8081/ws'),
      reconnectDelay: 5000,
    });

    this.client.onConnect = () => {
      this.connected = true;
      console.log('✅ Connecté au WebSocket');

      // Abonnement direct au topic global des logs
      this.subscribeToGlobalLogs();
    };

    this.client.onStompError = (frame) => {
      console.error('❌ Erreur STOMP :', frame);
    };

    this.client.activate();
  }

  private subscribeToGlobalLogs() {
    this.client.subscribe(`/topic/logs/`, (message: IMessage) => {
      const body = JSON.parse(message.body);
      this.logsSubject.next(body);
    });
  }

  /** 🔴 Temps réel (global, sans appName) */
  getLogs(): Observable<any> {
    return this.logsSubject.asObservable();
  }

  /** 🔵 Historique (garde appName) */
  getPreviousLogs(appName: string): Observable<any[]> {
    return this.http.get<any[]>(`http://localhost:8081/api/applications/logs/${appName}`);
  }

  /** 🟢 Logs filtrés (garde appName) */
getLogsFiltered(level: string | null, startDate: string | null, endDate: string | null) {
  let params = new HttpParams();
  if (level) params = params.set('level', level);
  if (startDate) params = params.set('startDate', startDate);
  if (endDate) params = params.set('endDate', endDate);

  // 🔹 On fixe l'application à "appguard"
  const appName = 'AppGuard';
  return this.http.get<LogEntry[]>(`http://localhost:8081/api/applications/logs-filtered/${appName}`, { params });
}

}
