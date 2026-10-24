import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../application/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss'
})
export class LoginPageComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  email = '';
  senha = '';
  erro = signal('');
  carregando = signal(false);

  submeter(): void {
    this.erro.set('');
    this.carregando.set(true);

    this.authService.login({ email: this.email, senha: this.senha }).subscribe({
      next: () => {
        this.carregando.set(false);
        this.router.navigate(['/aplicacao']);
      },
      error: err => {
        this.carregando.set(false);
        this.erro.set(err?.error?.mensagem || err?.error?.erro || 'E-mail ou senha inválidos');
      }
    });
  }
}
