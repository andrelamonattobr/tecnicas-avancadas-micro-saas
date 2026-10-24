export interface Fornecedor {
  id: number;
  nome: string;
  nomeContato?: string;
  telefoneWhatsapp: string;
  documento?: string;
  email?: string;
  categoria?: string;
  ativo: boolean;
  dataCadastro: string;
  mensagem?: string;
}

export interface FornecedorCreateRequest {
  nome: string;
  nomeContato?: string;
  telefoneWhatsapp: string;
  documento?: string;
  email?: string;
  categoria?: string;
}

export interface FornecedorUpdateRequest {
  nome: string;
  nomeContato?: string;
  telefoneWhatsapp: string;
  documento?: string;
  email?: string;
  categoria?: string;
}
