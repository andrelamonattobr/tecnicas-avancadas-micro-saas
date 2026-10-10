import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ListaPedido,
  CriarListaPedidoDTO,
  RenomearListaPedidoDTO,
  ItemPedido,
  CriarItemPedidoDTO,
  AplicarDescontoDTO
} from '../models/pedido.model';

@Injectable({
  providedIn: 'root'
})
export class ListaPedidoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/listas';

  listar(empresaId?: string): Observable<ListaPedido[]> {
    const url = empresaId ? `${this.apiUrl}?empresaId=${encodeURIComponent(empresaId)}` : this.apiUrl;
    return this.http.get<ListaPedido[]>(url);
  }

  buscarPorId(id: string): Observable<ListaPedido> {
    return this.http.get<ListaPedido>(`${this.apiUrl}/${id}`);
  }

  criar(dto: CriarListaPedidoDTO): Observable<ListaPedido> {
    return this.http.post<ListaPedido>(this.apiUrl, dto);
  }

  renomear(id: string, dto: RenomearListaPedidoDTO): Observable<ListaPedido> {
    return this.http.patch<ListaPedido>(`${this.apiUrl}/${id}/titulo`, dto);
  }

  adicionarItem(listaId: string, dto: CriarItemPedidoDTO): Observable<ItemPedido> {
    return this.http.post<ItemPedido>(`${this.apiUrl}/${listaId}/itens`, dto);
  }

  listarItens(listaId: string): Observable<ItemPedido[]> {
    return this.http.get<ItemPedido[]>(`${this.apiUrl}/${listaId}/itens`);
  }

  removerItem(listaId: string, itemId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${listaId}/itens/${itemId}`);
  }

  aplicarDesconto(listaId: string, dto: AplicarDescontoDTO): Observable<ListaPedido> {
    return this.http.put<ListaPedido>(`${this.apiUrl}/${listaId}/desconto`, dto);
  }

  exportarMarkdown(listaId: string): Observable<string> {
    return this.http.get(`${this.apiUrl}/${listaId}/exportar-markdown`, {
      responseType: 'text'
    });
  }
}
