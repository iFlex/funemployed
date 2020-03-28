import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AppComponent } from './app.component';
import { OfficeComponent } from './bits/office/office.component';
import { GameLobbyComponent } from './bits/gamelobby/gamelobby.component';


const routes: Routes = [
  { path: 'gamelobby', component:GameLobbyComponent},
  { path: 'office', component:OfficeComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
