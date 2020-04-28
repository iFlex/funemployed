import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { GameCommService } from '../../services/gamecomm.service';
import { GameLobbyService } from '../../bits/gamelobby/gamelobby.service';
import { VirtualTimeScheduler } from 'rxjs';

@Component({
    selector: 'joingame-root',
    templateUrl: './joingame.component.html',
    styleUrls: ['./joingame.component.scss']
})

export class JoingameComponent implements OnInit {
    gameId: String;
    playerId: String;
    
    constructor(private router: Router, 
                private gameComm: GameCommService,
                private gameLobby: GameLobbyService) {                  
      this.gameId = "";
      this.playerId = "";
    }

    ngOnInit() {
    
    }
    
    joinGame() {
      this.gameComm.joinGame(this.gameId, this.playerId).subscribe((data)=>{
        console.log(data);
        
        this.gameLobby.gameId = this.gameId;
        this.gameLobby.playerId = this.playerId;
        this.router.navigate(['/gamelobby']);
      });
    }
}