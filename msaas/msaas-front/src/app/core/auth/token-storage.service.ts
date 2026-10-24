import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthResponse } from '../../domains/comerciante/domain/comerciante.model';

const TOKEN_KEY = 'msaas_jwt_token';
const ESTABELECIMENTO_KEY = 'msaas_nome_estabelecimento';
const EMAIL_KEY = 'msaas_email';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  private readonly platformId = inject(PLATFORM_ID);

  salvarSessao(auth: AuthResponse): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(TOKEN_KEY, auth.token);
      localStorage.setItem(ESTABELECIMENTO_KEY, auth.nomeEstabelecimento);
      localStorage.setItem(EMAIL_KEY, auth.email);
    }
  }

  obterToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem(TOKEN_KEY);
    }
    return null;
  }

  obterNomeEstabelecimento(): string {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem(ESTABELECIMENTO_KEY) || 'Meu Estabelecimento';
    }
    return 'Meu Estabelecimento';
  }

  estaAutenticado(): boolean {
    if (!isPlatformBrowser(this.platformId)) {
      return true;
    }
    return !!this.obterToken();
  }

  limparSessao(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(ESTABELECIMENTO_KEY);
      localStorage.removeItem(EMAIL_KEY);
    }
  }
}
