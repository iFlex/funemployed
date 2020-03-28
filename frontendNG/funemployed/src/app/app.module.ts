import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { OfficeComponent } from './bits/office/office.component';
import { GameLobbyComponent } from './bits/gamelobby/gamelobby.component';
import { RouterModule } from '@angular/router';
import { MenuComponent } from './menu.component';
import { RulesComponent } from './bits/rules/rules.component';

@NgModule({
  declarations: [
    AppComponent,
    OfficeComponent,
    GameLobbyComponent,
    RulesComponent,
    MenuComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    RouterModule.forRoot([
    { path: 'gamelobby', component: GameLobbyComponent },
    { path: 'office', component: OfficeComponent },
    { path: 'rules', component: RulesComponent }
  ])
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
