import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { ServiceStatus } from '../../models/ServiceStatus';

import SockJS from 'sockjs-client';
import { Client, over, Message } from 'stompjs';

@Injectable({
  providedIn: 'root'
})
export class ServicestatusService {
  private baseUrl = 'http://localhost:8081/api/servicesStatus'; 

  private stompClient: Client;
  private statusSubject = new BehaviorSubject<ServiceStatus | null>(null);
  public status$ = this.statusSubject.asObservable();

  constructor(private http: HttpClient) {
    // 🔹 Connexion WebSocket et abonnement au topic
    const socket = new SockJS('http://localhost:8081/ws');
    this.stompClient = over(socket);

    this.stompClient.connect({}, () => {
      console.log('✅ Connecté au WebSocket');

      this.stompClient.subscribe('/topic/metrics', (message: Message) => {
        const status: ServiceStatus = JSON.parse(message.body);
        console.log('📥 Message WebSocket reçu :', status);

        // 🔹 Mets à jour le BehaviorSubject pour que le composant reçoive les données
        this.statusSubject.next(status);
      });
    });
  }
  getMetricsByServiceName(serviceName: string): Observable<ServiceStatus[]> {
return this.http.get<ServiceStatus[]>(`${this.baseUrl}/${serviceName}`);  }

 // Méthode pour fermer le WebSocket
  closeWebSocket(): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect(() => {
        console.log('❌ WebSocket fermé');
      });
    }
  }
} 