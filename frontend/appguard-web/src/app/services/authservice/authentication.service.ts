import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs';
import { Authentification } from '../../models/Authentification';
import { catchError } from 'rxjs/operators';
@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private apiUrl = 'http://localhost:8081/api/users'; 

  constructor(private http: HttpClient) { }
   // Méthode pour se connecter

  login(auth: Authentification): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { username: auth.username,
      password: auth.password })
      .pipe(
        tap(response => {
          localStorage.setItem('accessToken', response.accessToken);
          localStorage.setItem('refreshToken', response.refreshToken);
        })
      );
  }
  
  //  méthode refreshToken dans le AuthService
refreshToken() {
  const refreshToken = localStorage.getItem('refreshToken');
  
  if (!refreshToken) {
    throw new Error('Refresh token not available');
  }

  return this.http.post<any>('/api/auth/refresh', { refreshToken }).pipe(
    catchError((error) => {
      console.error('Refresh token error:', error);
      throw error;
    })
  );
}
 // Déconnexion
 logout(): Observable<void> {
  return this.http.post<void>(`${this.apiUrl}/logout`, {});
  localStorage.clear();
}
  
}
