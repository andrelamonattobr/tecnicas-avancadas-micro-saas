import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-fornecedor-form-page',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './fornecedor-form-page.component.html',
  styleUrl: './fornecedor-form-page.component.scss'
})
export class FornecedorFormPageComponent {
  modoEdicao = signal(false);
  fornecedorId = '';

  nome = '';
  nomeContato = '';
  telefoneWhatsapp = '';
  documento = '';
  email = '';
  categoria = '';

  carregando = signal(false);
  erro = signal('');
  sucesso = signal('');

  salvar(): void {}
}
