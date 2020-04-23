import { Injectable } from '@angular/core';
import { GameCommService } from '../../services/gamecomm.service';
import { VirtualTimeScheduler } from 'rxjs';
import { runInThisContext } from 'vm';

@Injectable({
  providedIn: 'root'
})
export class GameService {
  public gameId: String;
  public playerId: String; //this is the owner of the lobby
  public wins:Array<Object>;

  public role: String;
  public employer: String;

  public cards: Object[];
  public ready: Boolean;
  public candidates: Object[];
  public card_deck;
  public job_deck;
  public players;
  public interviewed;
  public interviewInProgress: Boolean;
  public current_candidate;
  /*
  id:String (player_id)
  display_items: String[] (items to print in the candidate slice)
  ready: Boolean (if the player is ready)
  */

  constructor(private gamecomm: GameCommService) {
    this.gameId = null;
    this.playerId = null;
    this.cards = [];
    this.ready = false;
    this.wins = [];
    this.card_deck = {};
    this.job_deck = {};

    this.updatePeriodically(this);
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

  public getJobCardById(id){
    //return this.job_deck[parseInt(id)];
  }

  public getTraitCardById(id){
    let idint = parseInt(id)
    for(let key in this.cards){
      if(idint == this.cards[key]['id']){
        return this.cards[key];
      }
    }
    return {id:-1,'text':''}
    //return this.card_deck[parseInt(id)];
  }

  public intersect(setA, setB){
    let aMap = {};
    let bMap = {};
    for(let i in setA){
      aMap[setA[i]['id']] = true;
    }
    for(let i in setB){
      let id = setB[i]['id']; 
      bMap[id] = true;
    }

    //Add what's in B but not in A
    for(let i in setB){
      let id = setB[i]['id'];
      if(aMap[id] == null){
        setA.push(setB[i]);
      } 
    }

    //Remove what's in A but not in B
    for(let i  = 0; i < setA.length; ++i){
      let id = setA[i]['id'];
      if(bMap[id] == null){
        setA.splice(i,1)
        i--;
      }
    }
  }


  public updateCandidateCards(player, key){
    //populate board with statuses
    let ditems = []
    
    for(let kid in player.candidate_cards){
      let card = player.candidate_cards[kid];
      if(card.revealed == true || key == this.playerId){
        ditems.push(card)
      }
    }
    
    if(ditems.length == 0) { 
      if(player.ready == false) { 
        ditems.push({id:-1,text:"..."});
      } else {
        ditems.push({id:-1,text:"Ready"});
      }
    }
    this.candidates.push({id:key, display_items:ditems, wins: player.won.length});
  }

  public updateState() {
    this.gamecomm.status(this.gameId).subscribe((data) => {
      console.log(data);
      try {
        this.employer = data['current_employer']['id'];
        this.role = data['current_role']['text']
      } catch(e) {
        console.log(e);
      }

      this.players = data['players'];
      this.interviewed = data['players_interviewed'];
      this.interviewInProgress = data['interview_in_progress'];
      this.current_candidate = data['current_candidate'];

      this.candidates = [];
      
      for(let key in this.players){
        if(key == this.playerId){
          this.ready = this.players[key]['ready'];
          this.wins = this.players[key]['won'];
          this.intersect(this.cards, this.players[key]['traits']);
        }
        if(key != this.employer ) {
          this.updateCandidateCards(this.players[key], key);
        }
      }
    });
  }

  public allPlayersPresented(){
    return Object.keys(this.interviewed).length == Object.keys(this.players).length - 1;
  }

  public isInterviewInProgress(){
    return this.interviewInProgress;
  }

  public startInterview(playerId){
    this.gamecomm.startInterview(this.gameId, playerId).subscribe((data) => {
      if(data['success'] == true){
        console.log(data);
        alert("Interview Started:" + playerId);
      }
    },(error) => {
      alert(JSON.stringify(error))
    })
  }

  public revealCard(cardId){
    this.gamecomm.revealCard(this.gameId, this.playerId, cardId).subscribe((data) => {
      if(data['success'] == true){
        console.log(data);
      }
    },(error) => {
      alert(JSON.stringify(error))
    })
  }

  public endInterview(){
    this.gamecomm.endInterview(this.gameId).subscribe((data) => {
      console.log(data);
    },(error) => {
      alert(JSON.stringify(error))
    })
  }
  
  public declareWinner(id){
    this.gamecomm.declareTurnWinner(this.gameId, id).subscribe((data) => {
      console.log(data);
      alert("Winner:"+id);
    },(error) => {
      alert(JSON.stringify(error))
    })
  }

  public startTurn(){
      this.gamecomm.newTurn(this.gameId).subscribe((data)=>{
      },(error) => {
        alert(JSON.stringify(error))
      });
  }

  public updatePeriodically(ctx){
    console.log("GAME_TICK");
    ctx.updateState();
    setTimeout(()=>{ctx.updatePeriodically(ctx)}, 2000);
  }

  public toggleReady(cards){
    if(this.ready == false){
      this.gamecomm.readyUp(this.gameId, this.playerId, cards).subscribe((data) => {
        console.log(data);
      },(error) => {
        alert(JSON.stringify(error))
      })
    } else {
      this.gamecomm.unready(this.gameId, this.playerId).subscribe((data) => {
        console.log(data);
      },(error) => {
        alert(JSON.stringify(error))
      })
    }
  }

  public printGameState(){
    console.log("GAME STATE ------------ ");
    console.log("Employer: " + this.employer + " Role: "+ this.role);
    console.log("Candidates:");
    console.log(this.candidates);
    console.log("Cards in hand:");
    console.log(this.cards);
    console.log("----------------------- ");
  }
}