export interface RegistroComercianteRequest {
  nomeResponsavel: string;
  nomeEstabelecimento: string;
  email: string;
  senha: string;
  telefone?: string;
  documento?: string;
}

export interface RegistroComercianteResponse {
  id: number;
  nomeEstabelecimento: string;
  email: string;
  mensagem: string;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface AuthResponse {
  id: number;
  nomeEstabelecimento: string;
  email: string;
  token: string;
}
