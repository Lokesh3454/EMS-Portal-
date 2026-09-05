import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AiResponse } from '../models/ai.model';

@Injectable({
  providedIn: 'root'
})
export class AiService {
  private apiUrl = `${environment.apiUrl}/ai`;

  constructor(private http: HttpClient) {}

  askAssistant(query: string, contextPage?: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/query`, {
      query,
      contextPage: contextPage || window.location.pathname
    });
  }
}
