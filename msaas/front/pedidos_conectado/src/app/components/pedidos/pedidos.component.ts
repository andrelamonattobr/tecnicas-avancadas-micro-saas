import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FornecedorService } from '../../services/fornecedor.service';
import { ListaPedidoService } from '../../services/lista-pedido.service';
import { Fornecedor, CriarFornecedorDTO } from '../../models/fornecedor.model';
import { ListaPedido, CriarItemPedidoDTO } from '../../models/pedido.model';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pedidos.component.html',
  styleUrl: './pedidos.component.css'
})
export class PedidosComponent implements OnInit {
  private readonly fornecedorService = inject(FornecedorService);
  private readonly listaPedidoService = inject(ListaPedidoService);

  // Estados reativos
  readonly fornecedores = signal<Fornecedor[]>([]);
  readonly listas = signal<ListaPedido[]>([]);
  readonly listaSelecionada = signal<ListaPedido | null>(null);

  fornecedorFiltroId = '';
  
  // Formulários
  novoFornecedor: CriarFornecedorDTO = {
    razaoSocial: '',
    nomeFantasia: '',
    cnpj: '',
    contato: ''
  };

  novaLista = {
    empresaId: '',
    titulo: ''
  };

  novoTituloLista = '';

  novoItem: CriarItemPedidoDTO = {
    descricao: '',
    quantidade: 1,
    precoUnitario: 0
  };

  descontoInput = 0;
  markdownExportado = '';
  mensagemSucesso = '';
  mensagemErro = '';

  ngOnInit(): void {
    this.carregarFornecedores();
    this.carregarListas();
  }

  carregarFornecedores(): void {
    this.fornecedorService.listarTodos().subscribe({
      next: (dados) => this.fornecedores.set(dados),
      error: (err) => this.definirErro('Erro ao carregar fornecedores: ' + (err.error?.mensagem || err.message))
    });
  }

  carregarListas(empresaId?: string): void {
    this.listaPedidoService.listar(empresaId).subscribe({
      next: (dados) => this.listas.set(dados),
      error: (err) => this.definirErro('Erro ao carregar listas: ' + (err.error?.mensagem || err.message))
    });
  }

  filtrarListasPorFornecedor(): void {
    this.carregarListas(this.fornecedorFiltroId || undefined);
  }

  cadastrarFornecedor(): void {
    this.limparMensagens();
    const rs = this.novoFornecedor.razaoSocial ? this.novoFornecedor.razaoSocial.trim() : '';
    if (rs.length < 3 || rs.length > 120) {
      this.definirErro('Razão social é obrigatória e deve ter entre 3 e 120 caracteres.');
      return;
    }

    this.fornecedorService.criar(this.novoFornecedor).subscribe({
      next: (criado) => {
        this.definirSucesso(`Fornecedor '${criado.razaoSocial}' cadastrado com sucesso!`);
        this.novoFornecedor = { razaoSocial: '', nomeFantasia: '', cnpj: '', contato: '' };
        this.novaLista.empresaId = criado.id;
        this.carregarFornecedores();
      },
      error: (err) => this.definirErro(err.error?.mensagem || 'Erro ao cadastrar fornecedor.')
    });
  }

  criarLista(): void {
    this.limparMensagens();
    if (!this.novaLista.empresaId) {
      this.definirErro('Selecione uma empresa fornecedora para criar a lista (RN-01).');
      return;
    }
    const titulo = this.novaLista.titulo ? this.novaLista.titulo.trim() : '';
    if (titulo.length < 3 || titulo.length > 100) {
      this.definirErro('Título da lista é obrigatório e deve ter entre 3 e 100 caracteres.');
      return;
    }

    this.listaPedidoService.criar(this.novaLista).subscribe({
      next: (criada) => {
        this.definirSucesso(`Lista '${criada.titulo}' criada com sucesso! (Status inicial: R$ 0,00)`);
        this.novaLista.titulo = '';
        this.carregarListas();
        this.selecionarLista(criada);
      },
      error: (err) => this.definirErro(err.error?.mensagem || 'Erro ao criar lista.')
    });
  }

  selecionarLista(lista: ListaPedido): void {
    this.limparMensagens();
    this.listaPedidoService.buscarPorId(lista.id).subscribe({
      next: (completa) => {
        this.listaSelecionada.set(completa);
        this.novoTituloLista = completa.titulo;
        this.descontoInput = completa.descontoReais;
        this.markdownExportado = '';
      },
      error: (err) => this.definirErro(err.error?.mensagem || 'Erro ao carregar detalhes da lista.')
    });
  }

  renomearLista(): void {
    const lista = this.listaSelecionada();
    if (!lista) return;

    this.limparMensagens();
    const titulo = this.novoTituloLista ? this.novoTituloLista.trim() : '';
    if (titulo.length < 3 || titulo.length > 100) {
      this.definirErro('Novo título da lista deve ter entre 3 e 100 caracteres.');
      return;
    }

    this.listaPedidoService.renomear(lista.id, { titulo }).subscribe({
      next: (atualizada) => {
        this.definirSucesso(`Lista renomeada para '${atualizada.titulo}' com sucesso! (CA-02)`);
        this.listaSelecionada.set(atualizada);
        this.carregarListas();
      },
      error: (err) => this.definirErro(err.error?.mensagem || 'Erro ao renomear lista.')
    });
  }

  adicionarItem(): void {
    const lista = this.listaSelecionada();
    if (!lista) return;

    this.limparMensagens();
    const desc = this.novoItem.descricao ? this.novoItem.descricao.trim() : '';
    if (desc.length < 2 || desc.length > 120) {
      this.definirErro('Descrição do item deve ter entre 2 e 120 caracteres.');
      return;
    }
    if (this.novoItem.quantidade <= 0) {
      this.definirErro('Quantidade do item deve ser estritamente maior que zero.');
      return;
    }
    if (this.novoItem.precoUnitario < 0) {
      this.definirErro('Preço unitário não pode ser negativo.');
      return;
    }

    this.listaPedidoService.adicionarItem(lista.id, {
      descricao: desc,
      quantidade: Number(this.novoItem.quantidade),
      precoUnitario: Number(this.novoItem.precoUnitario)
    }).subscribe({
      next: (itemSalvo) => {
        this.definirSucesso(`Item '${itemSalvo.descricao}' adicionado à lista com sucesso!`);
        this.novoItem = { descricao: '', quantidade: 1, precoUnitario: 0 };
        this.recarregarListaAtiva(lista.id);
      },
      error: (err) => this.definirErro(err.error?.mensagem || 'Erro ao adicionar item.')
    });
  }

  removerItem(itemId: string): void {
    const lista = this.listaSelecionada();
    if (!lista) return;

    this.limparMensagens();
    this.listaPedidoService.removerItem(lista.id, itemId).subscribe({
      next: () => {
        this.definirSucesso('Item removido com sucesso!');
        this.recarregarListaAtiva(lista.id);
      },
      error: (err) => this.definirErro(err.error?.mensagem || 'Erro ao remover item.')
    });
  }

  aplicarDesconto(): void {
    const lista = this.listaSelecionada();
    if (!lista) return;

    this.limparMensagens();
    if (this.descontoInput < 0) {
      this.definirErro('Desconto em R$ não pode ser negativo.');
      return;
    }

    this.listaPedidoService.aplicarDesconto(lista.id, { descontoReais: Number(this.descontoInput) }).subscribe({
      next: (atualizada) => {
        this.definirSucesso(`Desconto de R$ ${this.descontoInput.toFixed(2)} aplicado! Total líquido: R$ ${atualizada.totalLiquido.toFixed(2)} (Piso zero)`);
        this.listaSelecionada.set(atualizada);
        this.carregarListas();
      },
      error: (err) => this.definirErro(err.error?.mensagem || 'Erro ao aplicar desconto.')
    });
  }

  exportarMarkdown(): void {
    const lista = this.listaSelecionada();
    if (!lista) return;

    this.limparMensagens();
    this.listaPedidoService.exportarMarkdown(lista.id).subscribe({
      next: (md) => {
        this.markdownExportado = md;
        this.definirSucesso('Documento Markdown gerado com sucesso! (CA-06 / RN-07)');
      },
      error: (err) => this.definirErro('Erro ao exportar markdown: ' + (err.error?.mensagem || err.message))
    });
  }

  copiarMarkdown(): void {
    if (!this.markdownExportado) return;
    navigator.clipboard.writeText(this.markdownExportado).then(() => {
      this.definirSucesso('Conteúdo Markdown copiado para a área de transferência!');
    });
  }

  nomeFornecedor(empresaId: string): string {
    const f = this.fornecedores().find(forn => forn.id === empresaId);
    return f ? f.razaoSocial : empresaId;
  }

  private recarregarListaAtiva(listaId: string): void {
    this.listaPedidoService.buscarPorId(listaId).subscribe({
      next: (atualizada) => {
        this.listaSelecionada.set(atualizada);
        this.carregarListas();
      }
    });
  }

  private definirSucesso(msg: string): void {
    this.mensagemSucesso = msg;
    this.mensagemErro = '';
  }

  private definirErro(msg: string): void {
    this.mensagemErro = msg;
    this.mensagemSucesso = '';
  }

  private limparMensagens(): void {
    this.mensagemSucesso = '';
    this.mensagemErro = '';
  }
}
