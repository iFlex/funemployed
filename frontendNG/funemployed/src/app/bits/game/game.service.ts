import { Injectable } from '@angular/core';
import { GameCommService } from '../../services/gamecomm.service';

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private gameId: String;
  private playerId: String; //this is the owner of the lobby
  
  public role: String;
  public employer: String;
  public cards: Object[];
  public ready: Boolean;
  public candidates: Object[];
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
  }

  public setup(gameId: String, playerId: String){
    this.gameId = gameId;
    this.playerId = playerId;

    console.log("Sstarted the game("+this.gamecomm+") as:"+this.playerId);
    this.startTurn();
    this.updatePeriodically(this);
  }

  public getGameId(){
      return this.gameId;
  }

  public getPlayerId(){
      return this.playerId;
  }

  public updateState() {
    this.gamecomm.status(this.gameId).subscribe((data) => {
      console.log(data);
      this.employer = data['current_employer']['id'];
      this.role = data['current_role']
    });
  }

  public startTurn(){
      this.gamecomm.newTurn(this.gameId).subscribe((data)=>{
        console.log("START_TURN");
        console.log(data);

        this.candidates = [];
        this.employer = data['employer'];
        this.role = data['role'];
        
        let candidates = data['candidates'];
        for(let index in candidates){
          if(candidates[index]['player'] == this.playerId){
            for(let jdex in candidates[index]['new_cards']){
              this.cards.push(candidates[index]['new_cards'][jdex]);
            }
          }
          
          if(candidates[index]['player'] != this.employer){
            this.candidates.push({id:candidates[index]['player'],display_items:["Not Ready"]})
          }
        }
      });
  }

  public updatePeriodically(ctx){
    ctx.updateState();
    setTimeout(()=>{ctx.updateState(ctx)}, 2000);
  }

  public toggleReady(cards){
    if(this.ready == false){
      this.gamecomm.readyUp(this.gameId, this.playerId, cards).subscribe((data) => {
        console.log(data);
        if(data['status'] == 'ok'){
          this.ready = true;
        }
      })
    } else {
      this.gamecomm.unready(this.gameId, this.playerId).subscribe((data) => {
        console.log(data);
        if(data['status'] == 'ok'){
          this.ready = false;
        }
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