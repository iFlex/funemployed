import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})

export class GameCommService {
  //ToDo: make configurable
  endpoint = "http://feliks.ro:8001/api" // "http://localhost:8080/api";
  

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

  startInterview(gameId, playerId){
    let url = this.endpoint + "/" + gameId + "/interview-start/" + playerId;
    return this.http.get(url);
  }

  revealCard(gameId, playerId, cardId){
    let url = this.endpoint + "/" + gameId + "/interview-reveal/" + playerId + "/" + cardId;
    return this.http.get(url);
  }

  endInterview(gameId){
    let url = this.endpoint + "/" + gameId + "/interview-end";
    return this.http.get(url);
  }

  declareTurnWinner(gameId, playerId) {
    let url = this.endpoint + "/" + gameId + "/turn-end/" + playerId;
    return this.http.get(url);
  }

  leaveGame(gameId, playerId){
    let url = this.endpoint + "/" + gameId + "/player-remove/" + playerId;
    return this.http.get(url);
  }

  forceNewTurn(gameId){
    return this.http.get(this.endpoint + "/" + gameId + "/turn-start/force");
  }
}