import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Fornecedor } from '../../domain/fornecedor.model';

@Component({
  selector: 'app-fornecedor-lista-page',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './fornecedor-lista-page.component.html',
  styleUrl: './fornecedor-lista-page.component.scss'
})
export class FornecedorListaPageComponent {
  fornecedores = signal<Fornecedor[]>([]);
  paginaAtual = signal(0);
  tamanhoPagina = signal(10);
  totalElementos = signal(0);
  totalPaginas = signal(0);

  filtroAtivo = true;
  termoBusca = '';

  carregando = signal(false);
  erro = signal('');
  mensagemSucesso = signal('');

  carregarFornecedores(): void {}

  aplicarFiltro(): void {
    this.paginaAtual.set(0);
    this.carregarFornecedores();
  }

  mudarPagina(novaPagina: number): void {
    if (novaPagina >= 0 && novaPagina < this.totalPaginas()) {
      this.paginaAtual.set(novaPagina);
      this.carregarFornecedores();
    }
  }

  inativarFornecedor(_id: number): void {}

  reativarFornecedor(_id: number): void {}
}
