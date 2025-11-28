import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-run-list',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="page-container"><h1>Reconciliation Runs</h1></div>`
})
export class RunListComponent {}

