import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-aplicacao-layout',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './aplicacao-layout.component.html',
  styleUrl: './aplicacao-layout.component.scss'
})
export class AplicacaoLayoutComponent {
  menuAberto = signal(true);
  nomeEstabelecimento = signal('Meu Estabelecimento');

  alternarMenu(): void {
    this.menuAberto.update(aberto => !aberto);
  }

  sair(): void {}
}
