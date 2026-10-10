import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Fornecedor, CriarFornecedorDTO } from '../models/fornecedor.model';

@Injectable({
  providedIn: 'root'
})
export class FornecedorService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/fornecedores';

  listarTodos(): Observable<Fornecedor[]> {
    return this.http.get<Fornecedor[]>(this.apiUrl);
  }

  buscarPorId(id: string): Observable<Fornecedor> {
    return this.http.get<Fornecedor>(`${this.apiUrl}/${id}`);
  }

  criar(dto: CriarFornecedorDTO): Observable<Fornecedor> {
    return this.http.post<Fornecedor>(this.apiUrl, dto);
  }
}
