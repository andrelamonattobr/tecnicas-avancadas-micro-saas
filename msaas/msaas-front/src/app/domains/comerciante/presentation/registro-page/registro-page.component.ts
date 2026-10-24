import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../application/auth.service';

@Component({
  selector: 'app-registro-page',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './registro-page.component.html',
  styleUrl: './registro-page.component.scss'
})
export class RegistroPageComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  nomeResponsavel = '';
  nomeEstabelecimento = '';
  email = '';
  senha = '';
  telefone = '';
  documento = '';

  erro = signal('');
  sucesso = signal('');
  carregando = signal(false);

  submeter(): void {
    this.erro.set('');
    this.sucesso.set('');
    this.carregando.set(true);

    this.authService
      .registrar({
        nomeResponsavel: this.nomeResponsavel,
        nomeEstabelecimento: this.nomeEstabelecimento,
        email: this.email,
        senha: this.senha,
        telefone: this.telefone || undefined,
        documento: this.documento || undefined
      })
      .subscribe({
        next: res => {
          this.carregando.set(false);
          this.sucesso.set(res.mensagem || 'Conta criada com sucesso');
          setTimeout(() => this.router.navigate(['/login']), 1000);
        },
        error: err => {
          this.carregando.set(false);
          this.erro.set(
            err?.error?.mensagem || err?.error?.erro || 'Não foi possível criar a conta.'
          );
        }
      });
  }
}
