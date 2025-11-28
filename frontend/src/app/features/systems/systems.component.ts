import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-systems',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Source Systems</h1>
        <div class="actions">
          <button class="btn btn-primary" (click)="showAddModal = true">
            <span class="material-icons">add</span>
            Add System
          </button>
        </div>
      </div>
      
      <div class="systems-grid">
        @for (system of systems; track system.id) {
          <div class="system-card" [class.inactive]="!system.isActive">
            <div class="system-header">
              <div class="system-icon" [class]="system.systemType?.toLowerCase()">
                <span class="material-icons">{{ getSystemIcon(system.systemType) }}</span>
              </div>
              <div class="system-info">
                <h3>{{ system.systemName }}</h3>
                <span class="system-code">{{ system.systemCode }}</span>
              </div>
              <div class="system-status">
                <span class="status-dot" [class.active]="system.isActive"></span>
              </div>
            </div>
            
            <p class="system-description">{{ system.description || 'No description' }}</p>
            
            <div class="system-details">
              <div class="detail-item">
                <span class="label">Type</span>
                <span class="value">{{ system.systemType }}</span>
              </div>
              @if (system.host) {
                <div class="detail-item">
                  <span class="label">Host</span>
                  <span class="value mono">{{ system.host }}:{{ system.port }}</span>
                </div>
              }
              @if (system.databaseName) {
                <div class="detail-item">
                  <span class="label">Database</span>
                  <span class="value">{{ system.databaseName }}</span>
                </div>
              }
              @if (system.dataOwner) {
                <div class="detail-item">
                  <span class="label">Owner</span>
                  <span class="value">{{ system.dataOwner }}</span>
                </div>
              }
            </div>
            
            <div class="system-actions">
              <button class="btn btn-secondary" (click)="testConnection(system)">
                <span class="material-icons">sync</span>
                Test
              </button>
              <button class="btn btn-secondary" (click)="editSystem(system)">
                <span class="material-icons">edit</span>
                Edit
              </button>
              <button class="btn btn-secondary danger" (click)="deleteSystem(system)">
                <span class="material-icons">delete</span>
              </button>
            </div>
          </div>
        }
        @empty {
          <div class="empty-state">
            <span class="material-icons">storage</span>
            <h3>No Systems Configured</h3>
            <p>Add your first source or target system to get started.</p>
            <button class="btn btn-primary" (click)="showAddModal = true">
              <span class="material-icons">add</span>
              Add System
            </button>
          </div>
        }
      </div>
      
      <!-- Add/Edit Modal -->
      @if (showAddModal) {
        <div class="modal-overlay" (click)="showAddModal = false">
          <div class="modal" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h2>{{ editingSystem ? 'Edit System' : 'Add New System' }}</h2>
              <button class="close-btn" (click)="showAddModal = false">
                <span class="material-icons">close</span>
              </button>
            </div>
            
            <form (ngSubmit)="saveSystem()">
              <div class="form-grid">
                <div class="form-group">
                  <label>System Code</label>
                  <input type="text" [(ngModel)]="formData.systemCode" name="systemCode" required>
                </div>
                
                <div class="form-group">
                  <label>System Name</label>
                  <input type="text" [(ngModel)]="formData.systemName" name="systemName" required>
                </div>
                
                <div class="form-group full">
                  <label>Description</label>
                  <textarea [(ngModel)]="formData.description" name="description" rows="2"></textarea>
                </div>
                
                <div class="form-group">
                  <label>System Type</label>
                  <select [(ngModel)]="formData.systemType" name="systemType" required>
                    <option value="DATABASE">Database</option>
                    <option value="FILE_SYSTEM">File System</option>
                    <option value="API_ENDPOINT">API Endpoint</option>
                    <option value="SFTP">SFTP</option>
                  </select>
                </div>
                
                <div class="form-group">
                  <label>Data Owner</label>
                  <input type="text" [(ngModel)]="formData.dataOwner" name="dataOwner">
                </div>
                
                @if (formData.systemType === 'DATABASE') {
                  <div class="form-group">
                    <label>Host</label>
                    <input type="text" [(ngModel)]="formData.host" name="host">
                  </div>
                  
                  <div class="form-group">
                    <label>Port</label>
                    <input type="number" [(ngModel)]="formData.port" name="port">
                  </div>
                  
                  <div class="form-group">
                    <label>Database Name</label>
                    <input type="text" [(ngModel)]="formData.databaseName" name="databaseName">
                  </div>
                  
                  <div class="form-group">
                    <label>Schema</label>
                    <input type="text" [(ngModel)]="formData.schemaName" name="schemaName">
                  </div>
                  
                  <div class="form-group">
                    <label>Username</label>
                    <input type="text" [(ngModel)]="formData.username" name="username">
                  </div>
                  
                  <div class="form-group">
                    <label>Password</label>
                    <input type="password" [(ngModel)]="formData.password" name="password">
                  </div>
                }
                
                @if (formData.systemType === 'FILE_SYSTEM') {
                  <div class="form-group full">
                    <label>File Path</label>
                    <input type="text" [(ngModel)]="formData.filePath" name="filePath">
                  </div>
                }
                
                @if (formData.systemType === 'API_ENDPOINT') {
                  <div class="form-group full">
                    <label>API URL</label>
                    <input type="text" [(ngModel)]="formData.apiUrl" name="apiUrl">
                  </div>
                  
                  <div class="form-group full">
                    <label>API Key</label>
                    <input type="password" [(ngModel)]="formData.apiKey" name="apiKey">
                  </div>
                }
              </div>
              
              <div class="modal-actions">
                <button type="button" class="btn btn-secondary" (click)="showAddModal = false">Cancel</button>
                <button type="submit" class="btn btn-primary">
                  {{ editingSystem ? 'Update' : 'Create' }} System
                </button>
              </div>
            </form>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .systems-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
      gap: 24px;
    }
    
    .system-card {
      background: #1e293b;
      border-radius: 16px;
      padding: 24px;
      border: 1px solid #334155;
      transition: all 0.2s ease;
      
      &:hover {
        border-color: #2563eb;
      }
      
      &.inactive {
        opacity: 0.6;
      }
    }
    
    .system-header {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      margin-bottom: 16px;
    }
    
    .system-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .material-icons {
        font-size: 24px;
        color: white;
      }
      
      &.database { background: linear-gradient(135deg, #6366f1, #4f46e5); }
      &.file_system { background: linear-gradient(135deg, #22c55e, #16a34a); }
      &.api_endpoint { background: linear-gradient(135deg, #f97316, #ea580c); }
      &.sftp { background: linear-gradient(135deg, #06b6d4, #0891b2); }
    }
    
    .system-info {
      flex: 1;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #f1f5f9;
        margin-bottom: 4px;
      }
      
      .system-code {
        font-family: 'JetBrains Mono', monospace;
        font-size: 12px;
        color: #64748b;
      }
    }
    
    .system-status {
      .status-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        background: #64748b;
        
        &.active {
          background: #22c55e;
          box-shadow: 0 0 8px rgba(34, 197, 94, 0.5);
        }
      }
    }
    
    .system-description {
      font-size: 14px;
      color: #94a3b8;
      margin-bottom: 20px;
      line-height: 1.5;
    }
    
    .system-details {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
      margin-bottom: 20px;
      padding: 16px;
      background: #0f172a;
      border-radius: 8px;
    }
    
    .detail-item {
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
        font-size: 13px;
        color: #f1f5f9;
        
        &.mono {
          font-family: 'JetBrains Mono', monospace;
        }
      }
    }
    
    .system-actions {
      display: flex;
      gap: 8px;
      
      .btn {
        flex: 1;
        padding: 8px;
        font-size: 13px;
        
        &.danger {
          flex: none;
          
          &:hover {
            background: rgba(239, 68, 68, 0.1);
            border-color: #ef4444;
            
            .material-icons {
              color: #ef4444;
            }
          }
        }
      }
    }
    
    .empty-state {
      grid-column: 1 / -1;
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
    
    .modal-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }
    
    .modal {
      background: #1e293b;
      border-radius: 16px;
      width: 600px;
      max-height: 90vh;
      overflow: auto;
      border: 1px solid #334155;
    }
    
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 24px;
      border-bottom: 1px solid #334155;
      
      h2 {
        font-size: 18px;
        color: #f1f5f9;
      }
      
      .close-btn {
        background: transparent;
        border: none;
        cursor: pointer;
        padding: 4px;
        
        .material-icons {
          color: #64748b;
          font-size: 24px;
        }
        
        &:hover .material-icons {
          color: #f1f5f9;
        }
      }
    }
    
    form {
      padding: 24px;
    }
    
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
      
      .full {
        grid-column: 1 / -1;
      }
    }
    
    .modal-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 24px;
      padding-top: 24px;
      border-top: 1px solid #334155;
    }
  `]
})
export class SystemsComponent implements OnInit {
  systems: any[] = [];
  showAddModal = false;
  editingSystem: any = null;
  formData: any = {
    systemCode: '',
    systemName: '',
    description: '',
    systemType: 'DATABASE',
    host: '',
    port: 1521,
    databaseName: '',
    schemaName: '',
    username: '',
    password: '',
    filePath: '',
    apiUrl: '',
    apiKey: '',
    dataOwner: ''
  };

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadSystems();
  }

  loadSystems() {
    this.api.getSystems().subscribe({
      next: (response) => {
        this.systems = response.data || [];
      },
      error: (err) => {
        console.error('Failed to load systems:', err);
        // Mock data for demo
        this.systems = [
          {
            id: 1,
            systemCode: 'CORE_BANKING',
            systemName: 'Core Banking System',
            description: 'Main banking transaction system for all core operations',
            systemType: 'DATABASE',
            host: 'db-server-01',
            port: 1521,
            databaseName: 'COREDB',
            schemaName: 'BANKING',
            dataOwner: 'Treasury Team',
            isActive: true
          },
          {
            id: 2,
            systemCode: 'RISK_SYSTEM',
            systemName: 'Risk Management System',
            description: 'Enterprise risk management platform',
            systemType: 'DATABASE',
            host: 'db-server-02',
            port: 1521,
            databaseName: 'RISKDB',
            schemaName: 'RISK',
            dataOwner: 'Risk Team',
            isActive: true
          },
          {
            id: 3,
            systemCode: 'EXTERNAL_FEED',
            systemName: 'External Data Feed',
            description: 'Daily external data files from vendors',
            systemType: 'FILE_SYSTEM',
            filePath: '/data/external/feeds',
            dataOwner: 'Data Team',
            isActive: true
          }
        ];
      }
    });
  }

  getSystemIcon(type: string): string {
    const icons: Record<string, string> = {
      'DATABASE': 'storage',
      'FILE_SYSTEM': 'folder',
      'API_ENDPOINT': 'api',
      'SFTP': 'cloud_download'
    };
    return icons[type] || 'storage';
  }

  testConnection(system: any) {
    this.api.testConnection(system.id).subscribe({
      next: (response) => {
        alert(response.data ? 'Connection successful!' : 'Connection failed');
      },
      error: () => {
        alert('Connection test failed');
      }
    });
  }

  editSystem(system: any) {
    this.editingSystem = system;
    this.formData = { ...system };
    this.showAddModal = true;
  }

  deleteSystem(system: any) {
    if (confirm(`Are you sure you want to delete ${system.systemName}?`)) {
      this.api.deleteSystem(system.id).subscribe({
        next: () => this.loadSystems(),
        error: (err) => alert('Failed to delete system')
      });
    }
  }

  saveSystem() {
    const request = this.editingSystem
      ? this.api.updateSystem(this.editingSystem.id, this.formData)
      : this.api.createSystem(this.formData);

    request.subscribe({
      next: () => {
        this.showAddModal = false;
        this.editingSystem = null;
        this.resetForm();
        this.loadSystems();
      },
      error: (err) => alert('Failed to save system')
    });
  }

  resetForm() {
    this.formData = {
      systemCode: '',
      systemName: '',
      description: '',
      systemType: 'DATABASE',
      host: '',
      port: 1521,
      databaseName: '',
      schemaName: '',
      username: '',
      password: '',
      filePath: '',
      apiUrl: '',
      apiKey: '',
      dataOwner: ''
    };
  }
}

