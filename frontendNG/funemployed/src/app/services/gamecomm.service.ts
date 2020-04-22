import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})

export class GameCommService {
  //ToDo: make configurable
  endpoint = "http://localhost:8000/api"
  

  constructor(private http: HttpClient) {

  }
  
  newGame(){
    return this.http.get(this.endpoint + "/game-new");
  }

  joinGame(gameId, playerId){
    return this.http.get(this.endpoint + "/" + gameId + "/player-add/" + playerId);
  }

  status(gameId){
    return this.http.get(this.endpoint + "/" + gameId );
  }

  newTurn(gameId){
    return this.http.get(this.endpoint + "/" + gameId + "/turn-start");
  }

  readyUp(gameId, playerId, cards){
    let url = this.endpoint + "/" + gameId + "/player-ready/" + playerId;
    for(let i in cards){
        let card = cards[i];
        url += "/"+card;
    }

    return this.http.get(url);
  }

  unready(gameId, playerId){
    let url = this.endpoint + "/" + gameId + "/player-unready/" + playerId;
    return this.http.get(url);
  }
}