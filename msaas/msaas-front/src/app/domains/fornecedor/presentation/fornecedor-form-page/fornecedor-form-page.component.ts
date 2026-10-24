import { Component, OnInit, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FornecedorService } from '../../application/fornecedor.service';

@Component({
  selector: 'app-fornecedor-form-page',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './fornecedor-form-page.component.html',
  styleUrl: './fornecedor-form-page.component.scss'
})
export class FornecedorFormPageComponent implements OnInit {
  private readonly fornecedorService = inject(FornecedorService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);

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

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.modoEdicao.set(true);
      this.fornecedorId = idParam;
      if (isPlatformBrowser(this.platformId)) {
        this.carregarFornecedor(Number(idParam));
      }
    } else {
      this.modoEdicao.set(false);
      this.fornecedorId = '';
    }
  }

  private carregarFornecedor(id: number): void {
    this.carregando.set(true);
    this.fornecedorService.buscarPorId(id).subscribe({
      next: forn => {
        this.fornecedorId = String(forn.id);
        this.nome = forn.nome;
        this.nomeContato = forn.nomeContato || '';
        this.telefoneWhatsapp = forn.telefoneWhatsapp;
        this.documento = forn.documento || '';
        this.email = forn.email || '';
        this.categoria = forn.categoria || '';
        this.carregando.set(false);
      },
      error: err => {
        this.carregando.set(false);
        this.erro.set(
          err?.error?.mensagem || err?.error?.erro || 'Fornecedor não encontrado.'
        );
      }
    });
  }

  salvar(): void {
    this.erro.set('');
    this.sucesso.set('');
    this.carregando.set(true);

    if (this.modoEdicao()) {
      const id = Number(this.fornecedorId);
      this.fornecedorService
        .atualizar(id, {
          nome: this.nome,
          nomeContato: this.nomeContato || undefined,
          telefoneWhatsapp: this.telefoneWhatsapp,
          documento: this.documento || undefined,
          email: this.email || undefined,
          categoria: this.categoria || undefined
        })
        .subscribe({
          next: res => {
            this.carregando.set(false);
            this.sucesso.set(res.mensagem || 'Fornecedor atualizado com sucesso');
            setTimeout(() => this.router.navigate(['/aplicacao/fornecedores']), 800);
          },
          error: err => {
            this.carregando.set(false);
            this.erro.set(
              err?.error?.mensagem || err?.error?.erro || 'Não foi possível atualizar o fornecedor.'
            );
          }
        });
    } else {
      this.fornecedorService
        .criar({
          nome: this.nome,
          nomeContato: this.nomeContato || undefined,
          telefoneWhatsapp: this.telefoneWhatsapp,
          documento: this.documento || undefined,
          email: this.email || undefined,
          categoria: this.categoria || undefined
        })
        .subscribe({
          next: res => {
            this.carregando.set(false);
            this.sucesso.set(res.mensagem || 'Fornecedor cadastrado com sucesso');
            setTimeout(() => this.router.navigate(['/aplicacao/fornecedores']), 800);
          },
          error: err => {
            this.carregando.set(false);
            this.erro.set(
              err?.error?.mensagem || err?.error?.erro || 'Não foi possível cadastrar o fornecedor.'
            );
          }
        });
    }
  }
}
