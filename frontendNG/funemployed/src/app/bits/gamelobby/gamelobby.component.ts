import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { GameLobbyService } from './gamelobby.service';
import { GameService } from '../game/game.service';

@Component({
    selector: 'gamelobby-root',
    templateUrl: './gamelobby.component.html',
    styleUrls: ['./gamelobby.component.scss']
})

export class GameLobbyComponent implements OnInit {
    roomReady: boolean;
    constructor(private router: Router,
                public gameLobby: GameLobbyService,
                private gameService: GameService) {                  
      
    }

    ngOnInit() {
      this.roomReady = false;
      
      if(!this.gameLobby.doesLobbyExist()){
        this.gameLobby.createLobby().subscribe((data) =>{
          this.gameLobby.gameId = data["game_id"];  
        });
      } else {
        this.gameLobby.updateLobby();
      }
    }

    setReady(event: any) {
      console.log(event);
      // this.currentPlayer.ready = !this.currentPlayer.ready;

      // if (!this.currentPlayer.ready)
      // {
      //   this.roomReady = false;
      //   return;
      // }

      // for(var i = 0; i < this.playerStatuses.length; i++)
      // {
      //   if(!this.playerStatuses[i].ready) {
      //     this.roomReady = false;
      //     return;
      //   }
      // }

      this.roomReady = true;
    }

    startGame() {
      this.gameService.setup(this.gameLobby.gameId, this.gameLobby.playerId);
      this.router.navigate(['/game']);  // define your component where you want to go
    }
}