import { Component, OnInit, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Fornecedor } from '../../domain/fornecedor.model';
import { FornecedorService } from '../../application/fornecedor.service';

@Component({
  selector: 'app-fornecedor-lista-page',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './fornecedor-lista-page.component.html',
  styleUrl: './fornecedor-lista-page.component.scss'
})
export class FornecedorListaPageComponent implements OnInit {
  private readonly fornecedorService = inject(FornecedorService);
  private readonly platformId = inject(PLATFORM_ID);

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

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.carregarFornecedores();
    }
  }

  carregarFornecedores(): void {
    this.carregando.set(true);
    this.erro.set('');

    this.fornecedorService
      .listarPaginado(
        this.paginaAtual(),
        this.tamanhoPagina(),
        this.filtroAtivo,
        this.termoBusca
      )
      .subscribe({
        next: pagina => {
          this.fornecedores.set(pagina.content);
          this.paginaAtual.set(pagina.page);
          this.tamanhoPagina.set(pagina.size);
          this.totalElementos.set(pagina.totalElements);
          this.totalPaginas.set(pagina.totalPages);
          this.carregando.set(false);
        },
        error: err => {
          this.carregando.set(false);
          this.erro.set(
            err?.error?.mensagem || err?.error?.erro || 'Não foi possível carregar os fornecedores.'
          );
        }
      });
  }

  aplicarFiltro(): void {
    this.paginaAtual.set(0);
    this.mensagemSucesso.set('');
    this.carregarFornecedores();
  }

  mudarPagina(novaPagina: number): void {
    if (novaPagina >= 0 && novaPagina < this.totalPaginas()) {
      this.paginaAtual.set(novaPagina);
      this.carregarFornecedores();
    }
  }

  inativarFornecedor(id: number): void {
    this.erro.set('');
    this.mensagemSucesso.set('');

    this.fornecedorService.inativar(id).subscribe({
      next: res => {
        this.mensagemSucesso.set(res.mensagem || 'Fornecedor inativado com sucesso');
        this.carregarFornecedores();
      },
      error: err => {
        this.erro.set(
          err?.error?.mensagem || err?.error?.erro || 'Não foi possível inativar o fornecedor.'
        );
      }
    });
  }

  reativarFornecedor(id: number): void {
    this.erro.set('');
    this.mensagemSucesso.set('');

    this.fornecedorService.reativar(id).subscribe({
      next: res => {
        this.mensagemSucesso.set(res.mensagem || 'Fornecedor reativado com sucesso');
        this.carregarFornecedores();
      },
      error: err => {
        this.erro.set(
          err?.error?.mensagem || err?.error?.erro || 'Não foi possível reativar o fornecedor.'
        );
      }
    });
  }
}
