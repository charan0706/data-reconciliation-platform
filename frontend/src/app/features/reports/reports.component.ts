import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Reports & Analytics</h1>
        <div class="actions">
          <button class="btn btn-secondary">
            <span class="material-icons">download</span>
            Export Report
          </button>
        </div>
      </div>
      
      <div class="reports-grid">
        <div class="report-card">
          <div class="report-icon">
            <span class="material-icons">assessment</span>
          </div>
          <div class="report-info">
            <h3>Reconciliation Summary</h3>
            <p>Overview of all reconciliation runs and their outcomes</p>
          </div>
          <button class="btn btn-secondary">Generate</button>
        </div>
        
        <div class="report-card">
          <div class="report-icon">
            <span class="material-icons">warning</span>
          </div>
          <div class="report-info">
            <h3>Discrepancy Analysis</h3>
            <p>Detailed breakdown of discrepancies by type, severity, and system</p>
          </div>
          <button class="btn btn-secondary">Generate</button>
        </div>
        
        <div class="report-card">
          <div class="report-icon">
            <span class="material-icons">report_problem</span>
          </div>
          <div class="report-info">
            <h3>Incident Report</h3>
            <p>Status and resolution metrics for all incidents</p>
          </div>
          <button class="btn btn-secondary">Generate</button>
        </div>
        
        <div class="report-card">
          <div class="report-icon">
            <span class="material-icons">history</span>
          </div>
          <div class="report-info">
            <h3>Audit Trail</h3>
            <p>Complete audit log for regulatory compliance</p>
          </div>
          <button class="btn btn-secondary">Generate</button>
        </div>
        
        <div class="report-card">
          <div class="report-icon">
            <span class="material-icons">trending_up</span>
          </div>
          <div class="report-info">
            <h3>Trend Analysis</h3>
            <p>Historical trends and patterns in data quality</p>
          </div>
          <button class="btn btn-secondary">Generate</button>
        </div>
        
        <div class="report-card">
          <div class="report-icon">
            <span class="material-icons">speed</span>
          </div>
          <div class="report-info">
            <h3>Performance Metrics</h3>
            <p>System performance and SLA compliance reports</p>
          </div>
          <button class="btn btn-secondary">Generate</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .reports-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 24px;
    }
    
    .report-card {
      background: #1e293b;
      border-radius: 16px;
      padding: 24px;
      border: 1px solid #334155;
      display: flex;
      align-items: center;
      gap: 20px;
      transition: all 0.2s ease;
      
      &:hover {
        border-color: #2563eb;
      }
    }
    
    .report-icon {
      width: 56px;
      height: 56px;
      background: linear-gradient(135deg, #6366f1, #4f46e5);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      .material-icons {
        font-size: 28px;
        color: white;
      }
    }
    
    .report-info {
      flex: 1;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #f1f5f9;
        margin-bottom: 4px;
      }
      
      p {
        font-size: 13px;
        color: #94a3b8;
        line-height: 1.4;
      }
    }
    
    .btn {
      flex-shrink: 0;
    }
  `]
})
export class ReportsComponent {}

