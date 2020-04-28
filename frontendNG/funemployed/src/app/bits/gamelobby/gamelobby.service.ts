import { Injectable } from '@angular/core';
import { GameCommService } from '../../services/gamecomm.service';
import { Router } from '@angular/router';
import { GameService } from '../game/game.service';

@Injectable({
  providedIn: 'root'
})
export class GameLobbyService {
  public gameId: String;
  public playerId: String; //this is the owner of the lobby
  public players: String[];
  public polling:Boolean;
  public traitsCount;
  public jobsCount;

  constructor(private gamecomm: GameCommService, private router: Router, private gameService: GameService) {
    this.gameId = null;
    this.playerId = null;
    this.players = [];
    this.traitsCount = 0;
    this.jobsCount = 0;
    this.polling = false;

    this.enablePolling();
  }

  public getMaxRoundCount(){
    let maxRoundsByTraits = Math.floor((this.traitsCount - (this.players.length * 6)) / ((this.players.length - 1) * 3))
    return Math.min(this.jobsCount, maxRoundsByTraits);
  }

  public startGame(){
    this.gameService.setup(this.gameId, this.playerId);
    this.router.navigate(['/game']);  // define your component where you want to go
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
     
      this.players = data['players'];

      //check if the game has started
      if(data['turnInProgress'] == true){
        this.startGame();
        this.polling = false;
      }
    });
  }

  public createLobby() {
    return this.gamecomm.newGame();
  }

  public periodicUpdate(ctx){
    console.log("TICK");
    ctx.updateLobby();
    if(ctx.polling == true) {
      setTimeout(()=>{ctx.periodicUpdate(ctx)}, 2000);
    }
  }

  public enablePolling(){
    if(!this.polling) {
      console.log("ENABLED POLLING");
      this.polling = true;
      this.periodicUpdate(this);
    }
  }
}