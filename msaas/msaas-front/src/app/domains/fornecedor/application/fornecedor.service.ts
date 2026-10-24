import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Fornecedor,
  FornecedorCreateRequest,
  FornecedorUpdateRequest
} from '../domain/fornecedor.model';
import { Pagina } from '../domain/pagina.model';

@Injectable({
  providedIn: 'root'
})
export class FornecedorService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/fornecedores';

  listarPaginado(
    page: number,
    size: number,
    ativo: boolean,
    busca?: string
  ): Observable<Pagina<Fornecedor>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('ativo', ativo);

    if (busca && busca.trim().length > 0) {
      params = params.set('busca', busca.trim());
    }

    return this.http.get<Pagina<Fornecedor>>(this.apiUrl, { params });
  }

  buscarPorId(id: number): Observable<Fornecedor> {
    return this.http.get<Fornecedor>(`${this.apiUrl}/${id}`);
  }

  criar(request: FornecedorCreateRequest): Observable<Fornecedor> {
    return this.http.post<Fornecedor>(this.apiUrl, request);
  }

  atualizar(id: number, request: FornecedorUpdateRequest): Observable<Fornecedor> {
    return this.http.put<Fornecedor>(`${this.apiUrl}/${id}`, request);
  }

  inativar(id: number): Observable<Fornecedor> {
    return this.http.delete<Fornecedor>(`${this.apiUrl}/${id}`);
  }

  reativar(id: number): Observable<Fornecedor> {
    return this.http.patch<Fornecedor>(`${this.apiUrl}/${id}/reativar`, {});
  }
}
