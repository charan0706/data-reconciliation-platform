import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-config-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Reconciliation Configurations</h1>
        <div class="actions">
          <button class="btn btn-primary" (click)="showAddModal = true">
            <span class="material-icons">add</span>
            New Configuration
          </button>
        </div>
      </div>
      
      <div class="config-list">
        @for (config of configs; track config.id) {
          <div class="config-card" [routerLink]="['/reconciliation/config', config.id]">
            <div class="config-header">
              <div class="config-info">
                <h3>{{ config.configName }}</h3>
                <span class="config-code">{{ config.configCode }}</span>
              </div>
              <div class="config-status">
                @if (config.isScheduled) {
                  <span class="schedule-badge" [class.enabled]="config.scheduleEnabled">
                    <span class="material-icons">schedule</span>
                    {{ config.scheduleFrequency }}
                  </span>
                }
              </div>
            </div>
            
            <p class="config-description">{{ config.description || 'No description' }}</p>
            
            <div class="systems-flow">
              <div class="system-pill">
                <span class="material-icons">storage</span>
                {{ config.sourceSystemName }}
              </div>
              <span class="material-icons flow-arrow">arrow_forward</span>
              <div class="system-pill">
                <span class="material-icons">storage</span>
                {{ config.targetSystemName }}
              </div>
            </div>
            
            <div class="config-stats">
              <div class="stat">
                <span class="label">Last Run</span>
                <span class="value">{{ config.lastRunAt || 'Never' }}</span>
              </div>
              <div class="stat">
                <span class="label">Status</span>
                <span class="status-badge" [class]="getStatusClass(config.lastRunStatus)">
                  {{ config.lastRunStatus || 'N/A' }}
                </span>
              </div>
              <div class="stat">
                <span class="label">Discrepancies</span>
                <span class="value" [class.error]="config.lastRunDiscrepancies > 0">
                  {{ config.lastRunDiscrepancies || 0 }}
                </span>
              </div>
            </div>
            
            <div class="config-actions" (click)="$event.stopPropagation()">
              <button class="btn btn-primary" (click)="runReconciliation(config)">
                <span class="material-icons">play_arrow</span>
                Run Now
              </button>
              <button class="btn btn-secondary" (click)="editConfig(config)">
                <span class="material-icons">edit</span>
                Edit
              </button>
            </div>
          </div>
        }
        @empty {
          <div class="empty-state">
            <span class="material-icons">compare_arrows</span>
            <h3>No Configurations</h3>
            <p>Create your first reconciliation configuration to start comparing data.</p>
            <button class="btn btn-primary" (click)="showAddModal = true">
              <span class="material-icons">add</span>
              New Configuration
            </button>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .config-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    
    .config-card {
      background: #1e293b;
      border-radius: 16px;
      padding: 24px;
      border: 1px solid #334155;
      cursor: pointer;
      transition: all 0.2s ease;
      
      &:hover {
        border-color: #2563eb;
        transform: translateX(4px);
      }
    }
    
    .config-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 12px;
    }
    
    .config-info {
      h3 {
        font-size: 18px;
        font-weight: 600;
        color: #f1f5f9;
        margin-bottom: 4px;
      }
      
      .config-code {
        font-family: 'JetBrains Mono', monospace;
        font-size: 12px;
        color: #64748b;
      }
    }
    
    .schedule-badge {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 12px;
      background: #334155;
      border-radius: 8px;
      font-size: 12px;
      color: #64748b;
      
      .material-icons {
        font-size: 16px;
      }
      
      &.enabled {
        background: rgba(34, 197, 94, 0.15);
        color: #22c55e;
      }
    }
    
    .config-description {
      font-size: 14px;
      color: #94a3b8;
      margin-bottom: 20px;
    }
    
    .systems-flow {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;
      padding: 16px;
      background: #0f172a;
      border-radius: 8px;
    }
    
    .system-pill {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      background: #334155;
      border-radius: 8px;
      font-size: 13px;
      color: #f1f5f9;
      
      .material-icons {
        font-size: 18px;
        color: #64748b;
      }
    }
    
    .flow-arrow {
      color: #64748b;
    }
    
    .config-stats {
      display: flex;
      gap: 32px;
      margin-bottom: 20px;
    }
    
    .stat {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .label {
        font-size: 11px;
        color: #64748b;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
      
      .value {
        font-size: 14px;
        color: #f1f5f9;
        
        &.error {
          color: #ef4444;
        }
      }
    }
    
    .config-actions {
      display: flex;
      gap: 12px;
      
      .btn {
        padding: 10px 20px;
      }
    }
    
    .empty-state {
      text-align: center;
      padding: 64px 24px;
      background: #1e293b;
      border-radius: 16px;
      border: 1px dashed #334155;
      
      .material-icons {
        font-size: 64px;
        color: #334155;
        margin-bottom: 16px;
      }
      
      h3 {
        font-size: 18px;
        color: #f1f5f9;
        margin-bottom: 8px;
      }
      
      p {
        font-size: 14px;
        color: #64748b;
        margin-bottom: 24px;
      }
    }
  `]
})
export class ConfigListComponent implements OnInit {
  configs: any[] = [];
  showAddModal = false;

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadConfigs();
  }

  loadConfigs() {
    this.api.getConfigs().subscribe({
      next: (response) => {
        this.configs = response.data || [];
      },
      error: (err) => {
        console.error('Failed to load configs:', err);
        // Mock data
        this.configs = [
          {
            id: 1,
            configCode: 'RECON-001',
            configName: 'Core Banking vs Risk System',
            description: 'Daily reconciliation between core banking and risk management system',
            sourceSystemName: 'Core Banking System',
            targetSystemName: 'Risk Management System',
            isScheduled: true,
            scheduleFrequency: 'DAILY',
            scheduleEnabled: true,
            lastRunAt: '2024-01-15 08:00:00',
            lastRunStatus: 'COMPLETED_WITH_DISCREPANCIES',
            lastRunDiscrepancies: 23
          },
          {
            id: 2,
            configCode: 'RECON-002',
            configName: 'External Feed Validation',
            description: 'Validate external vendor data against internal records',
            sourceSystemName: 'External Data Feed',
            targetSystemName: 'Core Banking System',
            isScheduled: true,
            scheduleFrequency: 'DAILY',
            scheduleEnabled: true,
            lastRunAt: '2024-01-15 06:00:00',
            lastRunStatus: 'COMPLETED',
            lastRunDiscrepancies: 0
          }
        ];
      }
    });
  }

  runReconciliation(config: any) {
    this.api.triggerReconciliation(config.id).subscribe({
      next: () => alert('Reconciliation triggered successfully'),
      error: () => alert('Failed to trigger reconciliation')
    });
  }

  editConfig(config: any) {
    // Navigate to edit page
  }

  getStatusClass(status: string): string {
    const statusMap: Record<string, string> = {
      'COMPLETED': 'success',
      'COMPLETED_WITH_DISCREPANCIES': 'warning',
      'IN_PROGRESS': 'info',
      'FAILED': 'error'
    };
    return statusMap[status] || 'pending';
  }
}

