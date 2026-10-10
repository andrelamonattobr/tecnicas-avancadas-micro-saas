import { Routes } from '@angular/router';
import { PedidosComponent } from './components/pedidos/pedidos.component';

export const routes: Routes = [
  { path: '', component: PedidosComponent },
  { path: 'pedidos', component: PedidosComponent },
  { path: '**', redirectTo: '' }
];
