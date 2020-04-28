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
          this.gameLobby.gameId = data["id"];  
        });
      } else {
        this.gameLobby.updateLobby();
      }
    }

    startGame() {
      this.gameLobby.startGame();
    }
}