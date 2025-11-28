import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
  pageInfo?: PageInfo;
}

export interface PageInfo {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Systems
  getSystems(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/v1/systems`);
  }

  getSystem(id: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/v1/systems/${id}`);
  }

  createSystem(data: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/v1/systems`, data);
  }

  updateSystem(id: number, data: any): Observable<ApiResponse<any>> {
    return this.http.put<ApiResponse<any>>(`${this.baseUrl}/v1/systems/${id}`, data);
  }

  deleteSystem(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/v1/systems/${id}`);
  }

  testConnection(id: number): Observable<ApiResponse<boolean>> {
    return this.http.post<ApiResponse<boolean>>(`${this.baseUrl}/v1/systems/${id}/test-connection`, {});
  }

  // Reconciliation Configs
  getConfigs(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/v1/reconciliation/configs`);
  }

  getConfig(id: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/v1/reconciliation/configs/${id}`);
  }

  createConfig(data: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/v1/reconciliation/configs`, data);
  }

  updateConfig(id: number, data: any): Observable<ApiResponse<any>> {
    return this.http.put<ApiResponse<any>>(`${this.baseUrl}/v1/reconciliation/configs/${id}`, data);
  }

  deleteConfig(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/v1/reconciliation/configs/${id}`);
  }

  // Reconciliation Runs
  triggerReconciliation(configId: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/v1/reconciliation/configs/${configId}/run`, {});
  }

  getRuns(page = 0, size = 20): Observable<ApiResponse<any[]>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/v1/reconciliation/runs`, { params });
  }

  getRun(runId: string): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/v1/reconciliation/runs/${runId}`);
  }

  getRunsByConfig(configId: number): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/v1/reconciliation/configs/${configId}/runs`);
  }

  getRunDiscrepancies(runId: string): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/v1/reconciliation/runs/${runId}/discrepancies`);
  }

  // Incidents
  getIncidents(page = 0, size = 20): Observable<ApiResponse<any[]>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/v1/incidents`, { params });
  }

  getIncident(id: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/v1/incidents/${id}`);
  }

  assignIncident(id: number, userId: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/v1/incidents/${id}/assign?userId=${userId}`, {});
  }

  startInvestigation(id: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/v1/incidents/${id}/start-investigation`, {});
  }

  submitResolution(id: number, data: any): Observable<ApiResponse<any>> {
    const params = new HttpParams()
      .set('rootCause', data.rootCause)
      .set('proposedResolution', data.proposedResolution)
      .set('resolutionNotes', data.resolutionNotes || '');
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/v1/incidents/${id}/submit-resolution`, {}, { params });
  }

  approveResolution(id: number, comments: string): Observable<ApiResponse<any>> {
    const params = new HttpParams().set('comments', comments);
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/v1/incidents/${id}/approve`, {}, { params });
  }

  rejectResolution(id: number, reason: string): Observable<ApiResponse<any>> {
    const params = new HttpParams().set('rejectionReason', reason);
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/v1/incidents/${id}/reject`, {}, { params });
  }

  // Dashboard
  getDashboard(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/v1/dashboard`);
  }

  getConfigSummary(configId: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/v1/dashboard/config/${configId}/summary`);
  }
}

