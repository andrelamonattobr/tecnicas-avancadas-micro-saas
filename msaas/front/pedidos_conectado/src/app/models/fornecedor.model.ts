export interface Fornecedor {
  id: string;
  razaoSocial: string;
  nomeFantasia?: string;
  cnpj?: string;
  contato?: string;
}

export interface CriarFornecedorDTO {
  razaoSocial: string;
  nomeFantasia?: string;
  cnpj?: string;
  contato?: string;
}
