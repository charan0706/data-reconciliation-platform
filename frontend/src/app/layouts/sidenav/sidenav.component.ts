import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  badge?: number;
}

@Component({
  selector: 'app-sidenav',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="layout">
      <aside class="sidebar">
        <div class="logo">
          <div class="logo-icon">
            <span class="material-icons">compare_arrows</span>
          </div>
          <div class="logo-text">
            <span class="title">DataRecon</span>
            <span class="subtitle">Platform</span>
          </div>
        </div>
        
        <nav class="nav">
          @for (item of navItems; track item.route) {
            <a 
              class="nav-item" 
              [routerLink]="item.route"
              routerLinkActive="active">
              <span class="material-icons">{{ item.icon }}</span>
              <span class="label">{{ item.label }}</span>
              @if (item.badge) {
                <span class="badge">{{ item.badge }}</span>
              }
            </a>
          }
        </nav>
        
        <div class="sidebar-footer">
          <div class="user-info">
            <div class="avatar">
              <span class="material-icons">person</span>
            </div>
            <div class="details">
              <span class="name">Admin User</span>
              <span class="role">Administrator</span>
            </div>
          </div>
        </div>
      </aside>
      
      <main class="main-content">
        <header class="topbar">
          <div class="search-box">
            <span class="material-icons">search</span>
            <input type="text" placeholder="Search reconciliations, incidents...">
          </div>
          <div class="topbar-actions">
            <button class="icon-btn" title="Notifications">
              <span class="material-icons">notifications</span>
              <span class="notification-dot"></span>
            </button>
            <button class="icon-btn" title="Settings">
              <span class="material-icons">settings</span>
            </button>
          </div>
        </header>
        
        <div class="content">
          <ng-content></ng-content>
        </div>
      </main>
    </div>
  `,
  styles: [`
    .layout {
      display: flex;
      height: 100vh;
      background: #0f172a;
    }
    
    .sidebar {
      width: 260px;
      background: #1e293b;
      border-right: 1px solid #334155;
      display: flex;
      flex-direction: column;
    }
    
    .logo {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 20px 24px;
      border-bottom: 1px solid #334155;
    }
    
    .logo-icon {
      width: 42px;
      height: 42px;
      background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .material-icons {
        color: white;
        font-size: 24px;
      }
    }
    
    .logo-text {
      display: flex;
      flex-direction: column;
      
      .title {
        font-size: 18px;
        font-weight: 700;
        color: #f1f5f9;
      }
      
      .subtitle {
        font-size: 12px;
        color: #64748b;
      }
    }
    
    .nav {
      flex: 1;
      padding: 16px 12px;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    
    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      border-radius: 8px;
      color: #94a3b8;
      text-decoration: none;
      transition: all 0.2s ease;
      
      .material-icons {
        font-size: 20px;
      }
      
      .label {
        font-size: 14px;
        font-weight: 500;
      }
      
      .badge {
        margin-left: auto;
        background: #dc2626;
        color: white;
        font-size: 11px;
        font-weight: 600;
        padding: 2px 8px;
        border-radius: 9999px;
      }
      
      &:hover {
        background: #334155;
        color: #f1f5f9;
      }
      
      &.active {
        background: rgba(37, 99, 235, 0.15);
        color: #2563eb;
        
        .material-icons {
          color: #2563eb;
        }
      }
    }
    
    .sidebar-footer {
      padding: 16px;
      border-top: 1px solid #334155;
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px;
      border-radius: 8px;
      cursor: pointer;
      transition: background 0.2s ease;
      
      &:hover {
        background: #334155;
      }
    }
    
    .avatar {
      width: 36px;
      height: 36px;
      background: #334155;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .material-icons {
        font-size: 20px;
        color: #94a3b8;
      }
    }
    
    .details {
      display: flex;
      flex-direction: column;
      
      .name {
        font-size: 14px;
        font-weight: 500;
        color: #f1f5f9;
      }
      
      .role {
        font-size: 12px;
        color: #64748b;
      }
    }
    
    .main-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }
    
    .topbar {
      height: 64px;
      background: #1e293b;
      border-bottom: 1px solid #334155;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 24px;
    }
    
    .search-box {
      display: flex;
      align-items: center;
      gap: 8px;
      background: #0f172a;
      border: 1px solid #334155;
      border-radius: 8px;
      padding: 8px 16px;
      width: 400px;
      
      .material-icons {
        color: #64748b;
        font-size: 20px;
      }
      
      input {
        flex: 1;
        background: transparent;
        border: none;
        outline: none;
        color: #f1f5f9;
        font-size: 14px;
        
        &::placeholder {
          color: #64748b;
        }
      }
    }
    
    .topbar-actions {
      display: flex;
      gap: 8px;
    }
    
    .icon-btn {
      position: relative;
      width: 40px;
      height: 40px;
      background: transparent;
      border: 1px solid #334155;
      border-radius: 8px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      
      .material-icons {
        color: #94a3b8;
        font-size: 20px;
      }
      
      &:hover {
        background: #334155;
        
        .material-icons {
          color: #f1f5f9;
        }
      }
    }
    
    .notification-dot {
      position: absolute;
      top: 8px;
      right: 8px;
      width: 8px;
      height: 8px;
      background: #dc2626;
      border-radius: 50%;
    }
    
    .content {
      flex: 1;
      overflow: auto;
    }
  `]
})
export class SidenavComponent {
  navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { label: 'Systems', icon: 'storage', route: '/systems' },
    { label: 'Reconciliation', icon: 'compare_arrows', route: '/reconciliation' },
    { label: 'Incidents', icon: 'report_problem', route: '/incidents', badge: 5 },
    { label: 'Reports', icon: 'assessment', route: '/reports' }
  ];
}

