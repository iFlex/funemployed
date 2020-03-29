import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { GameLobbyService } from './gamelobby.service';

class PlayerStatus
{
  name: String
  ready: boolean
}

@Component({
    selector: 'gamelobby-root',
    templateUrl: './gamelobby.component.html',
    styleUrls: ['./gamelobby.component.scss']
})

export class GameLobbyComponent implements OnInit {
    roomReady: boolean;
    currentPlayer: PlayerStatus;
    playerStatuses: PlayerStatus[];
    constructor(private router: Router,
                private gamelobbyService: GameLobbyService) {                  
      this.currentPlayer = new PlayerStatus();
      this.currentPlayer.name = "NewPlayer1";
      this.currentPlayer.ready = false;

      this.playerStatuses = [];
    }

    ngOnInit() {
      this.roomReady = false;

      this.gamelobbyService.getGameLobbyStatus().subscribe((data)=>{
        console.log(data);
        this.playerStatuses = data['players'];
      });
    }

    setReady(event: any) {
      console.log(event);
      this.currentPlayer.ready = !this.currentPlayer.ready;

      if (!this.currentPlayer.ready)
      {
        this.roomReady = false;
        return;
      }

      for(var i = 0; i < this.playerStatuses.length; i++)
      {
        if(!this.playerStatuses[i].ready) {
          this.roomReady = false;
          return;
        }
      }

      this.roomReady = true;
    }

    startGame() {
      this.router.navigate(['/office']);  // define your component where you want to go
    }
}