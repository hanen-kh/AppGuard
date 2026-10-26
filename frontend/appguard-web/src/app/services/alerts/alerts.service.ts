import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Subject, Observable } from 'rxjs';
import * as StompJs from '@stomp/stompjs'; 
import SockJS from 'sockjs-client';
import { NewEntityAlert } from '../../models/NewEntityAlert';
@Injectable({
  providedIn: 'root'
})
export class AlertsService {
private client: Client;
  private connected = false;
  private alertsSubject = new Subject<NewEntityAlert>(); // Typé avec NewEntityAlert
  private topicSubscription: any;

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8081/ws'),
      reconnectDelay: 5000,
    });

    this.client.onConnect = () => {
      this.connected = true;
      console.log('✅ Connecté au WebSocket pour les alertes');
      this.subscribeToAlerts();
    };

    this.client.onStompError = (frame) => {
      console.error('❌ Erreur STOMP pour les alertes :', frame);
    };

    this.client.activate();
  }

  private subscribeToAlerts() {
    this.topicSubscription = this.client.subscribe('/topic/alerts', (message: IMessage) => {
      const body: NewEntityAlert = JSON.parse(message.body); // Parser en tant que NewEntityAlert
      this.alertsSubject.next(body);
      console.log('Nouvelle alerte reçue :', body);
    });
  }

  getAlerts(): Observable<NewEntityAlert> {
    return this.alertsSubject.asObservable();
  }

  unsubscribeFromAlerts(): void {
    if (this.topicSubscription) {
      this.topicSubscription.unsubscribe();
      this.topicSubscription = null;
      console.log('🔕 Désabonné du topic des alertes');
    }
  }
}