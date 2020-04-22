import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http'; 

import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { OfficeComponent } from './bits/office/office.component';
import { GameLobbyComponent } from './bits/gamelobby/gamelobby.component';
import { JoingameComponent } from './bits/joingame/joingame.component';
import { GameComponent } from './bits/game/game.component';
import { HighscoresComponent } from './bits/highscores/highscores.component';
import { RouterModule } from '@angular/router';
import { MenuComponent } from './menu.component';
import { RulesComponent } from './bits/rules/rules.component';

// primeng stuff
import { ButtonModule } from 'primeng/button';

@NgModule({
  declarations: [
    AppComponent,
    OfficeComponent,
    GameLobbyComponent,
    JoingameComponent,
    GameComponent,
    RulesComponent,
    MenuComponent,
  ],
  imports: [
    HttpClientModule,
    FormsModule,
    BrowserModule,
    AppRoutingModule,
    ButtonModule,
    RouterModule.forRoot([
    { path: 'gamelobby', component: GameLobbyComponent },
    { path: 'joingame', component: JoingameComponent },
    { path: 'game', component: GameComponent},
    { path: 'office', component: OfficeComponent },
    { path: 'rules', component: RulesComponent },
    { path: 'highscores', component: HighscoresComponent },
  ])
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
