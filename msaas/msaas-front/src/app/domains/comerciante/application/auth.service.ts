import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  AuthResponse,
  LoginRequest,
  RegistroComercianteRequest,
  RegistroComercianteResponse
} from '../domain/comerciante.model';
import { TokenStorageService } from '../../../core/auth/token-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly apiUrl = 'http://localhost:8080/api/auth';

  registrar(request: RegistroComercianteRequest): Observable<RegistroComercianteResponse> {
    return this.http.post<RegistroComercianteResponse>(`${this.apiUrl}/registro`, request);
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => this.tokenStorage.salvarSessao(response))
    );
  }

  logout(): Observable<{ mensagem: string }> {
    return this.http.post<{ mensagem: string }>(`${this.apiUrl}/logout`, {}).pipe(
      tap(() => this.tokenStorage.limparSessao())
    );
  }
}
