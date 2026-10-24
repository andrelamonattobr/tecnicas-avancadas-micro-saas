import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../comerciante/application/auth.service';
import { TokenStorageService } from '../../../../core/auth/token-storage.service';

@Component({
  selector: 'app-aplicacao-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './aplicacao-layout.component.html',
  styleUrl: './aplicacao-layout.component.scss'
})
export class AplicacaoLayoutComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly router = inject(Router);

  menuAberto = signal(true);
  nomeEstabelecimento = signal('Meu Estabelecimento');

  ngOnInit(): void {
    this.nomeEstabelecimento.set(this.tokenStorage.obterNomeEstabelecimento());
  }

  alternarMenu(): void {
    this.menuAberto.update(aberto => !aberto);
  }

  sair(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => {
        this.tokenStorage.limparSessao();
        this.router.navigate(['/login']);
      }
    });
  }
}
