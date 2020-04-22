import { Injectable } from '@angular/core';
import { GameCommService } from '../../services/gamecomm.service';

@Injectable({
  providedIn: 'root'
})
export class GameLobbyService {
  public gameId: String;
  public playerId: String; //this is the owner of the lobby
  public players: String[];

  constructor(private gamecomm: GameCommService) {
    this.gameId = null;
    this.playerId = null;
    this.players = [];
  }


  public doesLobbyExist(){
    if(this.gameId == null){
      return false;
    }

    return true;
  }

  public updateLobby() {
    this.gamecomm.status(this.gameId).subscribe((data) => {
      console.log(data);
      this.players = data['player_order'];
      console.log(this.players)
    });
  }

  public createLobby() {
    return this.gamecomm.newGame();
  }
}