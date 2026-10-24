import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-registro-page',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './registro-page.component.html',
  styleUrl: './registro-page.component.scss'
})
export class RegistroPageComponent {
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
  }
}
