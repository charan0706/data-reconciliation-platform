import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'systems',
    loadComponent: () => import('./features/systems/systems.component').then(m => m.SystemsComponent)
  },
  {
    path: 'reconciliation',
    children: [
      {
        path: '',
        loadComponent: () => import('./features/reconciliation/config-list/config-list.component').then(m => m.ConfigListComponent)
      },
      {
        path: 'config/:id',
        loadComponent: () => import('./features/reconciliation/config-detail/config-detail.component').then(m => m.ConfigDetailComponent)
      },
      {
        path: 'runs',
        loadComponent: () => import('./features/reconciliation/run-list/run-list.component').then(m => m.RunListComponent)
      },
      {
        path: 'run/:runId',
        loadComponent: () => import('./features/reconciliation/run-detail/run-detail.component').then(m => m.RunDetailComponent)
      }
    ]
  },
  {
    path: 'incidents',
    children: [
      {
        path: '',
        loadComponent: () => import('./features/incidents/incident-list/incident-list.component').then(m => m.IncidentListComponent)
      },
      {
        path: ':id',
        loadComponent: () => import('./features/incidents/incident-detail/incident-detail.component').then(m => m.IncidentDetailComponent)
      }
    ]
  },
  {
    path: 'reports',
    loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent)
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];

