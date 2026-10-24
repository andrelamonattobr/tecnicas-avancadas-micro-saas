import { Routes } from '@angular/router';
import { HomePageComponent } from './domains/comerciante/presentation/home-page/home-page.component';
import { LoginPageComponent } from './domains/comerciante/presentation/login-page/login-page.component';
import { RegistroPageComponent } from './domains/comerciante/presentation/registro-page/registro-page.component';
import { AplicacaoLayoutComponent } from './domains/aplicacao/presentation/aplicacao-layout/aplicacao-layout.component';
import { FornecedorListaPageComponent } from './domains/fornecedor/presentation/fornecedor-lista-page/fornecedor-lista-page.component';
import { FornecedorFormPageComponent } from './domains/fornecedor/presentation/fornecedor-form-page/fornecedor-form-page.component';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', component: HomePageComponent },
  { path: 'login', component: LoginPageComponent },
  { path: 'registro', component: RegistroPageComponent },
  {
    path: 'aplicacao',
    component: AplicacaoLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'fornecedores', pathMatch: 'full' },
      { path: 'fornecedores', component: FornecedorListaPageComponent },
      { path: 'fornecedores/novo', component: FornecedorFormPageComponent },
      { path: 'fornecedores/:id/editar', component: FornecedorFormPageComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];
