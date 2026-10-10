import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ListaPedidoService } from './lista-pedido.service';
import { ListaPedido, ItemPedido } from '../models/pedido.model';

describe('ListaPedidoService', () => {
  let service: ListaPedidoService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ListaPedidoService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ListaPedidoService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('deve criar lista de pedidos (POST /api/listas)', () => {
    const mockLista: ListaPedido = {
      id: 'l1',
      empresaId: 'f1',
      titulo: 'Reposição Outubro',
      dataCriacao: '2026-10-10T12:00:00Z',
      subtotalItens: 0,
      descontoReais: 0,
      totalLiquido: 0,
      itens: []
    };

    service.criar({ empresaId: 'f1', titulo: 'Reposição Outubro' }).subscribe((res) => {
      expect(res.id).toBe('l1');
      expect(res.subtotalItens).toBe(0);
      expect(res.totalLiquido).toBe(0);
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/listas');
    expect(req.request.method).toBe('POST');
    req.flush(mockLista);
  });

  it('deve renomear lista de pedidos (PATCH /api/listas/{id}/titulo)', () => {
    const mockAtualizada: ListaPedido = {
      id: 'l1',
      empresaId: 'f1',
      titulo: 'Pedido Final Aprovado',
      dataCriacao: '2026-10-10T12:00:00Z',
      subtotalItens: 0,
      descontoReais: 0,
      totalLiquido: 0,
      itens: []
    };

    service.renomear('l1', { titulo: 'Pedido Final Aprovado' }).subscribe((res) => {
      expect(res.titulo).toBe('Pedido Final Aprovado');
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/listas/l1/titulo');
    expect(req.request.method).toBe('PATCH');
    req.flush(mockAtualizada);
  });

  it('deve adicionar item à lista (POST /api/listas/{id}/itens)', () => {
    const mockItem: ItemPedido = {
      id: 'i1',
      listaId: 'l1',
      descricao: 'Parafuso',
      quantidade: 10,
      precoUnitario: 2,
      subtotal: 20
    };

    service.adicionarItem('l1', { descricao: 'Parafuso', quantidade: 10, precoUnitario: 2 }).subscribe((res) => {
      expect(res.subtotal).toBe(20);
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/listas/l1/itens');
    expect(req.request.method).toBe('POST');
    req.flush(mockItem);
  });

  it('deve aplicar desconto com piso zero (PUT /api/listas/{id}/desconto)', () => {
    const mockAtualizada: ListaPedido = {
      id: 'l1',
      empresaId: 'f1',
      titulo: 'Lista',
      dataCriacao: '2026-10-10T12:00:00Z',
      subtotalItens: 100,
      descontoReais: 150,
      totalLiquido: 0,
      itens: []
    };

    service.aplicarDesconto('l1', { descontoReais: 150 }).subscribe((res) => {
      expect(res.totalLiquido).toBe(0);
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/listas/l1/desconto');
    expect(req.request.method).toBe('PUT');
    req.flush(mockAtualizada);
  });

  it('deve exportar markdown (GET /api/listas/{id}/exportar-markdown)', () => {
    const mockMarkdown = '# Pedido de Compra: Teste';

    service.exportarMarkdown('l1').subscribe((md) => {
      expect(md).toContain('# Pedido de Compra:');
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/listas/l1/exportar-markdown');
    expect(req.request.method).toBe('GET');
    req.flush(mockMarkdown);
  });
});
