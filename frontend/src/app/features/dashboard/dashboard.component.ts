import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Dashboard</h1>
        <div class="actions">
          <button class="btn btn-secondary" (click)="refresh()">
            <span class="material-icons">refresh</span>
            Refresh
          </button>
        </div>
      </div>
      
      <!-- Key Metrics -->
      <div class="metrics-grid">
        <div class="metric-card">
          <div class="metric-icon systems">
            <span class="material-icons">storage</span>
          </div>
          <div class="metric-content">
            <span class="metric-label">Connected Systems</span>
            <span class="metric-value">{{ dashboard?.totalSystems || 0 }}</span>
          </div>
        </div>
        
        <div class="metric-card">
          <div class="metric-icon configs">
            <span class="material-icons">settings</span>
          </div>
          <div class="metric-content">
            <span class="metric-label">Active Configurations</span>
            <span class="metric-value">{{ dashboard?.totalConfigurations || 0 }}</span>
          </div>
        </div>
        
        <div class="metric-card">
          <div class="metric-icon runs">
            <span class="material-icons">play_circle</span>
          </div>
          <div class="metric-content">
            <span class="metric-label">Runs Today</span>
            <span class="metric-value">{{ dashboard?.totalRunsToday || 0 }}</span>
          </div>
        </div>
        
        <div class="metric-card">
          <div class="metric-icon incidents">
            <span class="material-icons">report_problem</span>
          </div>
          <div class="metric-content">
            <span class="metric-label">Open Incidents</span>
            <span class="metric-value error">{{ dashboard?.totalOpenIncidents || 0 }}</span>
          </div>
        </div>
      </div>
      
      <!-- Status Overview -->
      <div class="status-section">
        <div class="card match-rate">
          <h3>Average Match Rate</h3>
          <div class="rate-display">
            <div class="rate-circle" [style.--percentage]="dashboard?.averageMatchPercentage || 0">
              <span class="rate-value">{{ (dashboard?.averageMatchPercentage || 0) | number:'1.1-1' }}%</span>
            </div>
          </div>
          <div class="rate-labels">
            <div class="label-item">
              <span class="dot success"></span>
              <span>Matched</span>
            </div>
            <div class="label-item">
              <span class="dot error"></span>
              <span>Discrepancies</span>
            </div>
          </div>
        </div>
        
        <div class="card incident-breakdown">
          <h3>Incident Breakdown</h3>
          <div class="breakdown-list">
            <div class="breakdown-item">
              <span class="severity-dot critical"></span>
              <span class="label">Critical</span>
              <span class="value">{{ dashboard?.criticalIncidents || 0 }}</span>
            </div>
            <div class="breakdown-item">
              <span class="severity-dot high"></span>
              <span class="label">High</span>
              <span class="value">{{ dashboard?.highSeverityIncidents || 0 }}</span>
            </div>
            <div class="breakdown-item">
              <span class="severity-dot pending"></span>
              <span class="label">Pending Review</span>
              <span class="value">{{ dashboard?.pendingReviewIncidents || 0 }}</span>
            </div>
            <div class="breakdown-item">
              <span class="severity-dot overdue"></span>
              <span class="label">Overdue</span>
              <span class="value">{{ dashboard?.overdueIncidents || 0 }}</span>
            </div>
          </div>
        </div>
        
        <div class="card performance">
          <h3>Performance Metrics</h3>
          <div class="performance-grid">
            <div class="perf-item">
              <span class="perf-label">Success Rate</span>
              <span class="perf-value success">{{ (dashboard?.successRate || 0) | number:'1.1-1' }}%</span>
            </div>
            <div class="perf-item">
              <span class="perf-label">Avg Execution Time</span>
              <span class="perf-value">{{ formatDuration(dashboard?.averageExecutionTime) }}</span>
            </div>
            <div class="perf-item">
              <span class="perf-label">Scheduled Runs</span>
              <span class="perf-value">{{ dashboard?.scheduledRunsCompleted || 0 }}</span>
            </div>
            <div class="perf-item">
              <span class="perf-label">Failed Runs</span>
              <span class="perf-value error">{{ dashboard?.scheduledRunsFailed || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Recent Activity -->
      <div class="activity-section">
        <div class="card recent-runs">
          <div class="card-header">
            <h3>Recent Reconciliation Runs</h3>
            <a routerLink="/reconciliation/runs" class="view-all">View All</a>
          </div>
          <table class="data-table">
            <thead>
              <tr>
                <th>Run ID</th>
                <th>Configuration</th>
                <th>Status</th>
                <th>Discrepancies</th>
                <th>Started</th>
              </tr>
            </thead>
            <tbody>
              @for (run of dashboard?.recentRuns || []; track run.runId) {
                <tr>
                  <td class="mono">{{ run.runId }}</td>
                  <td>{{ run.configName }}</td>
                  <td>
                    <span class="status-badge" [class]="getStatusClass(run.status)">
                      {{ run.status }}
                    </span>
                  </td>
                  <td>{{ run.discrepancyCount }}</td>
                  <td>{{ run.startedAt }}</td>
                </tr>
              }
              @empty {
                <tr>
                  <td colspan="5" class="empty">No recent runs</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
        
        <div class="card recent-incidents">
          <div class="card-header">
            <h3>Recent Incidents</h3>
            <a routerLink="/incidents" class="view-all">View All</a>
          </div>
          <div class="incident-list">
            @for (incident of dashboard?.recentIncidents || []; track incident.id) {
              <div class="incident-item" [routerLink]="['/incidents', incident.id]">
                <div class="incident-header">
                  <span class="incident-number">{{ incident.incidentNumber }}</span>
                  <span class="status-badge" [class]="getStatusClass(incident.status)">
                    {{ incident.status }}
                  </span>
                </div>
                <div class="incident-title">{{ incident.title }}</div>
                <div class="incident-meta">
                  <span class="severity" [class]="'severity-' + incident.severity?.toLowerCase()">
                    {{ incident.severity }}
                  </span>
                  <span class="separator">•</span>
                  <span>{{ incident.discrepancyCount }} discrepancies</span>
                </div>
              </div>
            }
            @empty {
              <div class="empty-state">
                <span class="material-icons">check_circle</span>
                <p>No recent incidents</p>
              </div>
            }
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 24px;
      margin-bottom: 32px;
    }
    
    .metric-card {
      background: #1e293b;
      border-radius: 16px;
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 20px;
      border: 1px solid #334155;
      transition: all 0.2s ease;
      
      &:hover {
        border-color: #2563eb;
        transform: translateY(-2px);
      }
    }
    
    .metric-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .material-icons {
        font-size: 28px;
        color: white;
      }
      
      &.systems { background: linear-gradient(135deg, #6366f1, #4f46e5); }
      &.configs { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
      &.runs { background: linear-gradient(135deg, #06b6d4, #0891b2); }
      &.incidents { background: linear-gradient(135deg, #f97316, #ea580c); }
    }
    
    .metric-content {
      display: flex;
      flex-direction: column;
    }
    
    .metric-label {
      font-size: 13px;
      color: #94a3b8;
      margin-bottom: 4px;
    }
    
    .metric-value {
      font-size: 32px;
      font-weight: 700;
      color: #f1f5f9;
      
      &.error { color: #ef4444; }
    }
    
    .status-section {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr;
      gap: 24px;
      margin-bottom: 32px;
    }
    
    .card {
      background: #1e293b;
      border-radius: 16px;
      padding: 24px;
      border: 1px solid #334155;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #f1f5f9;
        margin-bottom: 20px;
      }
    }
    
    .rate-display {
      display: flex;
      justify-content: center;
      margin-bottom: 20px;
    }
    
    .rate-circle {
      width: 140px;
      height: 140px;
      border-radius: 50%;
      background: conic-gradient(
        #22c55e calc(var(--percentage) * 1%),
        #334155 0
      );
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      
      &::before {
        content: '';
        position: absolute;
        width: 100px;
        height: 100px;
        background: #1e293b;
        border-radius: 50%;
      }
      
      .rate-value {
        position: relative;
        font-size: 28px;
        font-weight: 700;
        color: #f1f5f9;
      }
    }
    
    .rate-labels {
      display: flex;
      justify-content: center;
      gap: 24px;
    }
    
    .label-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: #94a3b8;
      
      .dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        
        &.success { background: #22c55e; }
        &.error { background: #ef4444; }
      }
    }
    
    .breakdown-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    
    .breakdown-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background: #0f172a;
      border-radius: 8px;
      
      .label {
        flex: 1;
        font-size: 14px;
        color: #94a3b8;
      }
      
      .value {
        font-size: 18px;
        font-weight: 600;
        color: #f1f5f9;
      }
    }
    
    .severity-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      
      &.critical { background: #ef4444; }
      &.high { background: #f97316; }
      &.pending { background: #eab308; }
      &.overdue { background: #dc2626; }
    }
    
    .performance-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    
    .perf-item {
      padding: 16px;
      background: #0f172a;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    
    .perf-label {
      font-size: 12px;
      color: #64748b;
    }
    
    .perf-value {
      font-size: 20px;
      font-weight: 600;
      color: #f1f5f9;
      
      &.success { color: #22c55e; }
      &.error { color: #ef4444; }
    }
    
    .activity-section {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 24px;
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h3 {
        margin-bottom: 0;
      }
      
      .view-all {
        font-size: 13px;
        color: #2563eb;
      }
    }
    
    .incident-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    
    .incident-item {
      padding: 16px;
      background: #0f172a;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s ease;
      
      &:hover {
        background: #334155;
      }
    }
    
    .incident-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }
    
    .incident-number {
      font-family: 'JetBrains Mono', monospace;
      font-size: 12px;
      color: #64748b;
    }
    
    .incident-title {
      font-size: 14px;
      font-weight: 500;
      color: #f1f5f9;
      margin-bottom: 8px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .incident-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;
      color: #64748b;
      
      .separator {
        color: #334155;
      }
    }
    
    .status-badge {
      font-size: 10px;
      padding: 3px 8px;
    }
    
    .empty {
      text-align: center;
      color: #64748b;
      padding: 24px;
    }
    
    .empty-state {
      text-align: center;
      padding: 32px;
      color: #64748b;
      
      .material-icons {
        font-size: 48px;
        color: #22c55e;
        margin-bottom: 8px;
      }
    }
    
    @media (max-width: 1200px) {
      .metrics-grid {
        grid-template-columns: repeat(2, 1fr);
      }
      
      .status-section {
        grid-template-columns: 1fr;
      }
      
      .activity-section {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  dashboard: any = null;
  loading = true;

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadDashboard();
  }

  loadDashboard() {
    this.loading = true;
    this.api.getDashboard().subscribe({
      next: (response) => {
        this.dashboard = response.data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load dashboard:', err);
        this.loading = false;
        // Use mock data for demo
        this.dashboard = {
          totalSystems: 10,
          totalConfigurations: 25,
          totalRunsToday: 48,
          totalOpenIncidents: 12,
          averageMatchPercentage: 94.5,
          criticalIncidents: 2,
          highSeverityIncidents: 5,
          pendingReviewIncidents: 3,
          overdueIncidents: 1,
          successRate: 96.8,
          averageExecutionTime: 45000,
          scheduledRunsCompleted: 120,
          scheduledRunsFailed: 4,
          recentRuns: [],
          recentIncidents: []
        };
      }
    });
  }

  refresh() {
    this.loadDashboard();
  }

  getStatusClass(status: string): string {
    const statusMap: Record<string, string> = {
      'COMPLETED': 'success',
      'COMPLETED_WITH_DISCREPANCIES': 'warning',
      'IN_PROGRESS': 'info',
      'PENDING': 'pending',
      'FAILED': 'error',
      'OPEN': 'warning',
      'RESOLVED': 'success',
      'CLOSED': 'success'
    };
    return statusMap[status] || 'pending';
  }

  formatDuration(ms: number): string {
    if (!ms) return '0s';
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
    return `${(ms / 60000).toFixed(1)}m`;
  }
}

