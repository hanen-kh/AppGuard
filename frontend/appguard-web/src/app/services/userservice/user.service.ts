import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../../models/User';
import { HttpHeaders } from '@angular/common/http';
import { catchError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class UserService {
  private baseUrl = 'http://localhost:8081/api/users';

  constructor(private http:HttpClient) {}

   //  Ajouter un utilisateur
   createUser(user: User): Observable<User> {
    const url = `${this.baseUrl}/create_user`; 
  
    return this.http.post<User>(url, user).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.error && error.error.error) {
          return throwError(() => error.error.error); // Récupérer le message JSON de la réponse
        } else {
          return throwError(() => "An unexpected error occurred.");
        }
      })
    );
  }

  

  

   //  Obtenir tous les utilisateurs
   getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/all_users`);
  }

  //  Obtenir un utilisateur par ID
  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`);
  }

  //  Mettre à jour un utilisateur
  updateUser(id: number, user: User): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/${id}`, user);
  }

  //  Supprimer un utilisateur
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

 

  // Demande de nouveau mot de passe
  requestNewPassword(email: string): Observable<string> {
    return this.http.post(`${this.baseUrl}/requestNewPassword`, { email }, { responseType: 'text' }).pipe(
      catchError((error: HttpErrorResponse) => {
        const errorMsg = error.error || 'Une erreur est survenue.';
        return throwError(() => errorMsg);
      })
    );
  }
  
  // valider le code 
  validateCode(code: string, email: string): Observable<Map<string, string>> {
    const request = { code, email }; // Paramètres à envoyer au backend
    return this.http.post<Map<string, string>>(`${this.baseUrl}/validate-code`, request);
  }

  // changer le mot de passe
  changePassword(newPassword: string, email: string): Observable<string> {
    const request = { newPassword, email };
    return this.http.post(`${this.baseUrl}/change-password`, request, { responseType: 'text' });
  }
  
}
