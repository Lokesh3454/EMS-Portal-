import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EmployeeDocument } from '../models/document.model';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private apiUrl = `${environment.apiUrl}/documents`;

  constructor(private http: HttpClient) {}

  getMyDocuments(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/my-documents`);
  }

  getEmployeeDocuments(employeeId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/employee/${employeeId}`);
  }

  signDocument(documentId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${documentId}/sign`, {});
  }

  uploadDocument(dto: Partial<EmployeeDocument>): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/upload`, dto);
  }
}
