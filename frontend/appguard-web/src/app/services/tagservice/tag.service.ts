// src/app/services/tag.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Tag } from '../../models/tag';

@Injectable({
  providedIn: 'root'
})
export class TagService {

  private apiUrl = 'http://localhost:8081/api/tags'; // adapte l’URL à ton backend

  constructor(private http: HttpClient) {}

  // Récupérer tous les tags
  getAllTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.apiUrl);
  }

  // Récupérer un tag par ID
  getTagById(id: number): Observable<Tag> {
    return this.http.get<Tag>(`${this.apiUrl}/${id}`);
  }

  // Créer un nouveau tag
  createTag(tag: Tag): Observable<Tag> {
    return this.http.post<Tag>(this.apiUrl, tag);
  }

  // Mettre à jour un tag
  updateTag(id: number, tag: Tag): Observable<Tag> {
    return this.http.put<Tag>(`${this.apiUrl}/${id}`, tag);
  }

  // Supprimer un tag
  deleteTag(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  addTagToCronJob(tagId: number, cronJobId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${tagId}/cronjobs/${cronJobId}`, {});
  }
}
