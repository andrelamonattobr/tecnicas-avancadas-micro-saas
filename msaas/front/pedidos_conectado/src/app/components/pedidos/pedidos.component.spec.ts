import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { PedidosComponent } from './pedidos.component';
import { FornecedorService } from '../../services/fornecedor.service';
import { ListaPedidoService } from '../../services/lista-pedido.service';
import { Fornecedor } from '../../models/fornecedor.model';
import { ListaPedido } from '../../models/pedido.model';

describe('PedidosComponent', () => {
  let component: PedidosComponent;
  let fixture: ComponentFixture<PedidosComponent>;

  let mockFornecedorService: jasmine.SpyObj<FornecedorService> | any;
  let mockListaPedidoService: jasmine.SpyObj<ListaPedidoService> | any;

  const fornecedoresIniciais: Fornecedor[] = [
    { id: 'f1', razaoSocial: 'Distribuidora Nacional de Peças Ltda', cnpj: '12.345.678/0001-90' }
  ];

  const listasIniciais: ListaPedido[] = [
    {
      id: 'l1',
      empresaId: 'f1',
      titulo: 'Reposição Emergencial Outubro',
      dataCriacao: '2026-10-10T12:00:00Z',
      subtotalItens: 0,
      descontoReais: 0,
      totalLiquido: 0,
      itens: []
    }
  ];

  beforeEach(async () => {
    mockFornecedorService = {
      listarTodos: () => of(fornecedoresIniciais),
      criar: (dto: any) => of({ id: 'f2', ...dto })
    };

    mockListaPedidoService = {
      listar: () => of(listasIniciais),
      buscarPorId: (id: string) => of(listasIniciais[0]),
      criar: (dto: any) => of({ id: 'l2', ...dto, subtotalItens: 0, descontoReais: 0, totalLiquido: 0, itens: [] }),
      renomear: (id: string, dto: any) => of({ ...listasIniciais[0], titulo: dto.titulo }),
      adicionarItem: (listaId: string, dto: any) => of({ id: 'i1', listaId, ...dto, subtotal: dto.quantidade * dto.precoUnitario }),
      removerItem: () => of(void 0),
      aplicarDesconto: (id: string, dto: any) => of({ ...listasIniciais[0], descontoReais: dto.descontoReais, totalLiquido: 0 }),
      exportarMarkdown: () => of('# Pedido de Compra: Reposição Emergencial Outubro\n**Data de Criação:** ...')
    };

    await TestBed.configureTestingModule({
      imports: [PedidosComponent],
      providers: [
        { provide: FornecedorService, useValue: mockFornecedorService },
        { provide: ListaPedidoService, useValue: mockListaPedidoService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PedidosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve inicializar o componente e carregar fornecedores e listas', () => {
    expect(component).toBeTruthy();
    expect(component.fornecedores().length).toBe(1);
    expect(component.listas().length).toBe(1);
  });

  it('deve rejeitar cadastro de fornecedor com razão social menor que 3 caracteres', () => {
    component.novoFornecedor.razaoSocial = 'AB';
    component.cadastrarFornecedor();

    expect(component.mensagemErro).toContain('entre 3 e 120 caracteres');
  });

  it('RN-01: deve rejeitar criação de lista sem fornecedor selecionado', () => {
    component.novaLista.empresaId = '';
    component.novaLista.titulo = 'Título Válido';
    component.criarLista();

    expect(component.mensagemErro).toContain('Selecione uma empresa fornecedora');
  });

  it('CA-02: deve renomear lista selecionada dinamicamente', () => {
    component.selecionarLista(listasIniciais[0]);
    component.novoTituloLista = 'Pedido Final Aprovado - Diretoria';
    component.renomearLista();

    expect(component.listaSelecionada()?.titulo).toBe('Pedido Final Aprovado - Diretoria');
    expect(component.mensagemSucesso).toContain('CA-02');
  });

  it('deve rejeitar adição de item com quantidade menor ou igual a zero', () => {
    component.selecionarLista(listasIniciais[0]);
    component.novoItem = {
      descricao: 'Parafuso',
      quantidade: 0,
      precoUnitario: 10
    };
    component.adicionarItem();

    expect(component.mensagemErro).toContain('estritamente maior que zero');
  });

  it('CA-05: deve aplicar desconto fixo com sucesso', () => {
    component.selecionarLista(listasIniciais[0]);
    component.descontoInput = 50;
    component.aplicarDesconto();

    expect(component.mensagemSucesso).toContain('Desconto de R$ 50.00');
  });

  it('CA-06: deve exportar markdown com sucesso', () => {
    component.selecionarLista(listasIniciais[0]);
    component.exportarMarkdown();

    expect(component.markdownExportado).toContain('# Pedido de Compra:');
    expect(component.mensagemSucesso).toContain('CA-06');
  });
});
