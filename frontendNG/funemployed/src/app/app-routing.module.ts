import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AppComponent } from './app.component';
import { OfficeComponent } from './bits/office/office.component';
import { GameLobbyComponent } from './bits/gamelobby/gamelobby.component';
import { JoingameComponent } from './bits/joingame/joingame.component';
import { GameComponent } from './bits/game/game.component';

const routes: Routes = [
  { path: 'gamelobby', component:GameLobbyComponent},
  { path: 'joingame', component:JoingameComponent},
  { path: 'game', component: GameComponent},
  { path: 'office', component:OfficeComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
