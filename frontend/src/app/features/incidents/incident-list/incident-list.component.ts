import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-incident-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Incidents</h1>
        <div class="filters">
          <button class="filter-btn" [class.active]="activeFilter === 'all'" (click)="setFilter('all')">All</button>
          <button class="filter-btn" [class.active]="activeFilter === 'open'" (click)="setFilter('open')">Open</button>
          <button class="filter-btn" [class.active]="activeFilter === 'pending'" (click)="setFilter('pending')">Pending Review</button>
          <button class="filter-btn" [class.active]="activeFilter === 'resolved'" (click)="setFilter('resolved')">Resolved</button>
        </div>
      </div>
      
      <div class="incident-table">
        <table class="data-table">
          <thead>
            <tr>
              <th>Incident #</th>
              <th>Title</th>
              <th>Severity</th>
              <th>Status</th>
              <th>Assigned To</th>
              <th>Discrepancies</th>
              <th>Created</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            @for (incident of filteredIncidents; track incident.id) {
              <tr [routerLink]="['/incidents', incident.id]">
                <td class="mono">{{ incident.incidentNumber }}</td>
                <td>{{ incident.title }}</td>
                <td>
                  <span class="severity-badge" [class]="'severity-' + incident.severity?.toLowerCase()">
                    {{ incident.severity }}
                  </span>
                </td>
                <td>
                  <span class="status-badge" [class]="getStatusClass(incident.status)">
                    {{ formatStatus(incident.status) }}
                  </span>
                </td>
                <td>{{ incident.assignedToName || 'Unassigned' }}</td>
                <td>{{ incident.discrepancyCount }}</td>
                <td>{{ incident.createdAt }}</td>
                <td (click)="$event.stopPropagation()">
                  <div class="action-buttons">
                    @if (incident.status === 'OPEN') {
                      <button class="action-btn" title="Assign" (click)="assignIncident(incident)">
                        <span class="material-icons">person_add</span>
                      </button>
                    }
                    @if (incident.status === 'ASSIGNED') {
                      <button class="action-btn" title="Start Investigation" (click)="startInvestigation(incident)">
                        <span class="material-icons">search</span>
                      </button>
                    }
                    @if (incident.status === 'PENDING_CHECKER_REVIEW') {
                      <button class="action-btn success" title="Approve" (click)="approveIncident(incident)">
                        <span class="material-icons">check</span>
                      </button>
                      <button class="action-btn danger" title="Reject" (click)="rejectIncident(incident)">
                        <span class="material-icons">close</span>
                      </button>
                    }
                  </div>
                </td>
              </tr>
            }
            @empty {
              <tr>
                <td colspan="8" class="empty">No incidents found</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .filters {
      display: flex;
      gap: 8px;
    }
    
    .filter-btn {
      padding: 8px 16px;
      background: #334155;
      border: 1px solid #334155;
      border-radius: 8px;
      color: #94a3b8;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.2s ease;
      
      &:hover {
        background: #475569;
      }
      
      &.active {
        background: #2563eb;
        border-color: #2563eb;
        color: white;
      }
    }
    
    .incident-table {
      background: #1e293b;
      border-radius: 16px;
      border: 1px solid #334155;
      overflow: hidden;
    }
    
    .data-table {
      tr {
        cursor: pointer;
        
        &:hover td {
          background: rgba(37, 99, 235, 0.05);
        }
      }
    }
    
    .severity-badge {
      display: inline-flex;
      padding: 4px 10px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 600;
      text-transform: uppercase;
      
      &.severity-critical {
        background: rgba(239, 68, 68, 0.15);
        color: #ef4444;
      }
      
      &.severity-high {
        background: rgba(249, 115, 22, 0.15);
        color: #f97316;
      }
      
      &.severity-medium {
        background: rgba(234, 179, 8, 0.15);
        color: #eab308;
      }
      
      &.severity-low {
        background: rgba(6, 182, 212, 0.15);
        color: #06b6d4;
      }
    }
    
    .action-buttons {
      display: flex;
      gap: 4px;
    }
    
    .action-btn {
      width: 32px;
      height: 32px;
      background: transparent;
      border: 1px solid #334155;
      border-radius: 6px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      
      .material-icons {
        font-size: 18px;
        color: #94a3b8;
      }
      
      &:hover {
        background: #334155;
        
        .material-icons {
          color: #f1f5f9;
        }
      }
      
      &.success:hover {
        background: rgba(34, 197, 94, 0.15);
        border-color: #22c55e;
        
        .material-icons { color: #22c55e; }
      }
      
      &.danger:hover {
        background: rgba(239, 68, 68, 0.15);
        border-color: #ef4444;
        
        .material-icons { color: #ef4444; }
      }
    }
    
    .empty {
      text-align: center;
      color: #64748b;
      padding: 48px !important;
    }
  `]
})
export class IncidentListComponent implements OnInit {
  incidents: any[] = [];
  activeFilter = 'all';

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadIncidents();
  }

  loadIncidents() {
    this.api.getIncidents().subscribe({
      next: (response) => {
        this.incidents = response.data || [];
      },
      error: () => {
        // Mock data
        this.incidents = [
          {
            id: 1,
            incidentNumber: 'INC-20240115-0001',
            title: 'Data Discrepancies - Core Banking vs Risk - RUN-RECON-001-20240115',
            severity: 'HIGH',
            status: 'PENDING_CHECKER_REVIEW',
            assignedToName: 'John Analyst',
            discrepancyCount: 23,
            createdAt: '2024-01-15 08:30:00'
          },
          {
            id: 2,
            incidentNumber: 'INC-20240114-0003',
            title: 'Missing Records in Target System',
            severity: 'CRITICAL',
            status: 'UNDER_INVESTIGATION',
            assignedToName: 'Jane Smith',
            discrepancyCount: 156,
            createdAt: '2024-01-14 14:00:00'
          },
          {
            id: 3,
            incidentNumber: 'INC-20240114-0002',
            title: 'Balance Mismatch Detection',
            severity: 'MEDIUM',
            status: 'RESOLVED',
            assignedToName: 'Mike Johnson',
            discrepancyCount: 12,
            createdAt: '2024-01-14 10:00:00'
          }
        ];
      }
    });
  }

  get filteredIncidents() {
    if (this.activeFilter === 'all') return this.incidents;
    
    const statusFilters: Record<string, string[]> = {
      'open': ['OPEN', 'ASSIGNED', 'UNDER_INVESTIGATION'],
      'pending': ['PENDING_CHECKER_REVIEW'],
      'resolved': ['RESOLVED', 'CLOSED']
    };
    
    return this.incidents.filter(i => statusFilters[this.activeFilter]?.includes(i.status));
  }

  setFilter(filter: string) {
    this.activeFilter = filter;
  }

  getStatusClass(status: string): string {
    const statusMap: Record<string, string> = {
      'OPEN': 'warning',
      'ASSIGNED': 'info',
      'UNDER_INVESTIGATION': 'info',
      'PENDING_CHECKER_REVIEW': 'warning',
      'RESOLVED': 'success',
      'CLOSED': 'success',
      'CHECKER_REJECTED': 'error'
    };
    return statusMap[status] || 'pending';
  }

  formatStatus(status: string): string {
    return status?.replace(/_/g, ' ') || '';
  }

  assignIncident(incident: any) {
    // Show assign dialog
  }

  startInvestigation(incident: any) {
    this.api.startInvestigation(incident.id).subscribe({
      next: () => this.loadIncidents(),
      error: () => alert('Failed to start investigation')
    });
  }

  approveIncident(incident: any) {
    const comments = prompt('Enter approval comments:');
    if (comments) {
      this.api.approveResolution(incident.id, comments).subscribe({
        next: () => this.loadIncidents(),
        error: () => alert('Failed to approve')
      });
    }
  }

  rejectIncident(incident: any) {
    const reason = prompt('Enter rejection reason:');
    if (reason) {
      this.api.rejectResolution(incident.id, reason).subscribe({
        next: () => this.loadIncidents(),
        error: () => alert('Failed to reject')
      });
    }
  }
}

