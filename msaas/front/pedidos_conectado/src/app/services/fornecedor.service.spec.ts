import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { FornecedorService } from './fornecedor.service';
import { Fornecedor, CriarFornecedorDTO } from '../models/fornecedor.model';

describe('FornecedorService', () => {
  let service: FornecedorService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        FornecedorService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(FornecedorService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('deve listar todos os fornecedores (GET /api/fornecedores)', () => {
    const mockFornecedores: Fornecedor[] = [
      { id: 'f1', razaoSocial: 'Distribuidora A', nomeFantasia: 'A' },
      { id: 'f2', razaoSocial: 'Distribuidora B', nomeFantasia: 'B' }
    ];

    service.listarTodos().subscribe((dados) => {
      expect(dados.length).toBe(2);
      expect(dados[0].razaoSocial).toBe('Distribuidora A');
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/fornecedores');
    expect(req.request.method).toBe('GET');
    req.flush(mockFornecedores);
  });

  it('deve cadastrar um fornecedor (POST /api/fornecedores)', () => {
    const dto: CriarFornecedorDTO = {
      razaoSocial: 'Alimentos do Campo S.A.',
      nomeFantasia: 'Alimentos do Campo',
      cnpj: '12.345.678/0001-90',
      contato: 'contato@campo.com'
    };

    const mockCriado: Fornecedor = { id: 'f123', ...dto };

    service.criar(dto).subscribe((criado) => {
      expect(criado.id).toBe('f123');
      expect(criado.razaoSocial).toBe('Alimentos do Campo S.A.');
    });

    const req = httpTesting.expectOne('http://localhost:8080/api/fornecedores');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush(mockCriado);
  });
});
