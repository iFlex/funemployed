import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { OfficeComponent } from './bits/office/office.component';
import { GameLobbyComponent } from './bits/gamelobby/gamelobby.component';

@NgModule({
  declarations: [
    AppComponent,
    OfficeComponent,
    GameLobbyComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
