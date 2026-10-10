export interface ItemPedido {
  id: string;
  listaId: string;
  descricao: string;
  quantidade: number;
  precoUnitario: number;
  subtotal: number;
}

export interface CriarItemPedidoDTO {
  descricao: string;
  quantidade: number;
  precoUnitario: number;
}

export interface ListaPedido {
  id: string;
  empresaId: string;
  titulo: string;
  dataCriacao: string;
  subtotalItens: number;
  descontoReais: number;
  totalLiquido: number;
  itens: ItemPedido[];
}

export interface CriarListaPedidoDTO {
  empresaId: string;
  titulo: string;
}

export interface RenomearListaPedidoDTO {
  titulo: string;
}

export interface AplicarDescontoDTO {
  descontoReais: number;
}
