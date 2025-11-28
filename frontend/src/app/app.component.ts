import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidenavComponent } from './layouts/sidenav/sidenav.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, SidenavComponent],
  template: `
    <app-sidenav>
      <router-outlet></router-outlet>
    </app-sidenav>
  `,
  styles: [`
    :host {
      display: block;
      height: 100vh;
    }
  `]
})
export class AppComponent {
  title = 'Data Reconciliation Platform';
}

