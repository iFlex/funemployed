import { Injectable } from '@angular/core';
import { GameCommService } from '../../services/gamecomm.service';
import { Router } from '@angular/router';
import { MapType } from '@angular/compiler';

@Injectable({
  providedIn: 'root'
})

//ToDo: cleanup unnecessary state parameters
export class GameService {
  public gameId: String;
  public playerId: String; //this is the owner of the lobby
  public wins;

  public role: String;
  public employer: String;

  public cards: Object[];
  public ready: Boolean;
  public candidates: Object[];
  public players;
  public playerIds: string[];
  public interviewed;
  public interviewInProgress: Boolean;
  public current_candidate;
  public turnsPlayed;
  public turnsLeft;

  private winCallback: Function;
  private interviewStartCallback: Function;
  /*
  id:String (player_id)
  display_items: String[] (items to print in the candidate slice)
  ready: Boolean (if the player is ready)
  */

  constructor(private gamecomm: GameCommService, private router: Router) {
    this.gameId = "UNDEFINED";
    this.playerId = "UNDEFINED";
    this.cards = [];
    this.ready = false;
    this.wins = 0;
    this.players = {};
    this.turnsPlayed = 0;
    this.turnsLeft = 0;
    this.updatePeriodically(this);
  }

  public setWinnerCallback(func){
    this.winCallback = func;
  }

  public setInterviewStartCallback(func){
    this.interviewStartCallback = func;
  }

  public setup(gameId: String, playerId: String){
    this.gameId = gameId;
    this.playerId = playerId;

    console.log("Started the game("+this.gamecomm+") as:"+this.playerId);
    //this.startTurn();
  }

  public getGameId(){
    return this.gameId;
  }

  public getPlayerId(){
    return this.playerId;
  }

  public isCandidateReady(id){
    let player = this.players[id];
    if(player){
      return player.ready;
    }
    return false;
  }

  public isCurrentEmployer(id) {
    return (this.employer == id)
  }

  public isCurrentCandidate(id) {
    return (this.current_candidate && this.current_candidate['id'] == id)
  }

  public getPlayerCards() {
    let player = this.players[<string>this.playerId];
    if(player) {
      return player.traits;
    }
    return [];
  }

  public shallowUpdate(target, source) {
    for(let key in source){
      target[key] = source[key]
    }
  }

  public gracefulUpdate(setA, setB){
    let aMap = {};
    let bMap = {};
    let added = [];
    let removed = [];
    for(let i in setA){
      aMap[setA[i]['id']] = setA[i];
    }
    for(let i in setB){
      let id = setB[i]['id']; 
      bMap[id] = setB[i];
    }

    //Add what's in B but not in A
    for(let i in setB){
      let id = setB[i]['id'];
      if(aMap[id] == null){
        setA.push(setB[i]);
        added.push(setB[i]);
      } else {
        this.shallowUpdate(aMap[id],bMap[id]);
      }
    }

    //Remove what's in A but not in B
    for(let i  = 0; i < setA.length; ++i){
      let id = setA[i]['id'];
      if(bMap[id] == null){
        removed.push(setA[i])
        setA.splice(i,1)
        i--;
      }
    }

    return {"added":added,"removed":"removed"}
  }

  public translateToFrontendPlayerSturct(backendPlayer, frontendPlayer){
    if(frontendPlayer == null){
      frontendPlayer = {
        id:"",
        ready:false,
        traits:[],
        candidate_cards:[],
        won:[],
        wins:0
      }
    }

    frontendPlayer.id = backendPlayer.id;
    frontendPlayer.ready = backendPlayer.ready;
    this.gracefulUpdate(frontendPlayer.traits, backendPlayer.traits);
    this.gracefulUpdate(frontendPlayer.candidate_cards, backendPlayer.candidateCards);
    let winnings = this.gracefulUpdate(frontendPlayer.won, backendPlayer.wonCards);
    
    frontendPlayer.wins = frontendPlayer.won.length;
    frontendPlayer.interviewed = false;

    if(winnings.added.length > 0 && this.winCallback){
      this.winCallback(frontendPlayer, winnings.added[0]);
    }

    console.log(frontendPlayer);
    return frontendPlayer;
  }

  public updateState() {
    this.gamecomm.status(this.gameId).subscribe((data) => {
      console.log("GAME_TICK");
      console.log(data);
      if(data == null){
        console.log("Wadafac?");
        return;
      }
      
      let backendPlayers = data['players'];
      this.playerIds = [];
      for(let i in backendPlayers) {
        let backendPlayer = backendPlayers[i];
        let frontendPlayer = this.players[backendPlayer.id];
        let player = this.translateToFrontendPlayerSturct(backendPlayer, frontendPlayer);
        this.playerIds.push(player.id);

        if(frontendPlayer == null){
          this.players[player.id] = player;
        }
      }

      console.log("PLAYERS");
      console.log(this.players)

      try {
        this.employer = data['currentEmployer']['id'];
        this.role = data['currentRole']['text']
      } catch(e) {
        console.log(e);
      }

      this.interviewed = data['playersInterviewed'];
      this.interviewInProgress = data['interviewInProgress'];
      this.current_candidate = data['currentCandidate'];
      
      this.turnsPlayed = data['turnsPlayed'];
      this.turnsLeft = data['turnsLeft'];
    });
  }

  public getRevealedCardIds(id) {
    let player = this.players[id];
    let revealed = [];
    if(player){
      for(let i in player.candidate_cards){
        if(player.candidate_cards[i].revealed){
          revealed.push(player.candidate_cards[i].id);
        }
      }
    }
    return revealed;
  }

  public allPlayersPresented() {
    return this.interviewed.length == Object.keys(this.players).length - 1;
  }

  public isInterviewInProgress() {
    return this.interviewInProgress;
  }

  public hasPlayerInterviewed(id) {
    //return id in this.interviewed; //this didn't seem to work... curse you dynamically typed language masquerading as a typed one...
    for(let i in this.interviewed){
      if(this.interviewed[i] === id){
        return true;
      }
    }
    return false;
  }

  public extractErrorMessage(data) {
    if(data['error']) {
      console.error(data['error']);
      let msg = data['error']['errorMessage'];
      if(msg) {
        alert(msg);
      }
    }
    return null;
  }

  public startInterview(playerId){
    this.gamecomm.startInterview(this.gameId, playerId).subscribe((data) => {
      alert("Interview Started:" + playerId);
    },(error) => {
      this.extractErrorMessage(error)
    })
  }

  public revealCard(cardId){
    this.gamecomm.revealCard(this.gameId, this.playerId, cardId).subscribe((data) => {
    },(error) => {
      this.extractErrorMessage(error)
    })
  }

  public endInterview(){
    this.gamecomm.endInterview(this.gameId).subscribe((data) => {
      console.log(data);
    },(error) => {
      this.extractErrorMessage(error)
    })
  }
  
  public declareWinner(id){
    this.gamecomm.declareTurnWinner(this.gameId, id).subscribe((data) => {
      console.log(data);
    },(error) => {
      this.extractErrorMessage(error)
    })
  }

  public startTurn(){
      this.gamecomm.newTurn(this.gameId).subscribe((data)=>{
      },(error) => {
        this.extractErrorMessage(error)
      });
  }

  public updatePeriodically(ctx){
    ctx.updateState();
    setTimeout(()=>{ctx.updatePeriodically(ctx)}, 500);
  }

  public toggleReady(cards){
    if(this.ready == false){
      this.gamecomm.readyUp(this.gameId, this.playerId, cards).subscribe((data) => {
        console.log(data);
      },(error) => {
        this.extractErrorMessage(error)
      })
    } else {
      this.gamecomm.unready(this.gameId, this.playerId).subscribe((data) => {
        console.log(data);
      },(error) => {
        this.extractErrorMessage(error)
      })
    }
  }

  public leave(){
    this.gamecomm.leaveGame(this.gameId, this.playerId).subscribe((data) =>{
      this.router.navigate(['/joingame']);
    }, (error) => {
      this.router.navigate(['/joingame']);
    })
  }

  public forceNewTurn(){
    this.gamecomm.forceNewTurn(this.gameId).subscribe((data) => {
        console.log(data);
    })
  }

}