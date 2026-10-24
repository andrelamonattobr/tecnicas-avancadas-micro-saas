import { Routes } from '@angular/router';
import { HomePageComponent } from './domains/comerciante/presentation/home-page/home-page.component';
import { LoginPageComponent } from './domains/comerciante/presentation/login-page/login-page.component';
import { RegistroPageComponent } from './domains/comerciante/presentation/registro-page/registro-page.component';
import { AplicacaoLayoutComponent } from './domains/aplicacao/presentation/aplicacao-layout/aplicacao-layout.component';

export const routes: Routes = [
  { path: '', component: HomePageComponent },
  { path: 'login', component: LoginPageComponent },
  { path: 'registro', component: RegistroPageComponent },
  { path: 'aplicacao', component: AplicacaoLayoutComponent },
  { path: '**', redirectTo: '' }
];
