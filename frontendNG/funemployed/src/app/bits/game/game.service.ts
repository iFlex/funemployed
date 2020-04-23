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
  public card_deck;
  public job_deck;

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
    
    this.card_deck = {};
    this.job_deck = {};

    this.updatePeriodically(this);
  }

  public setup(gameId: String, playerId: String){
    this.gameId = gameId;
    this.playerId = playerId;

    console.log("Started the game("+this.gamecomm+") as:"+this.playerId);
    this.startTurn();
  }

  public getGameId(){
      return this.gameId;
  }

  public getPlayerId(){
      return this.playerId;
  }

  public update_job_deck(data){
    let jobs = data['jobs']
    for(let i in jobs){
      let key = jobs[i].id;
      this.job_deck[key] = jobs[i];
    }
  }

  public update_card_deck(data){
    let traits = data['traits'];
    for(let i in traits){
      let key = traits[i].id;
      this.card_deck[key] = traits[i];
    }
    
    console.log("CARDS");
    console.log(this.card_deck);
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

  public updateState() {
    this.gamecomm.status(this.gameId).subscribe((data) => {
      console.log(data);
      this.employer = data['current_employer']['id'];
      this.role = data['current_role']['text']

      this.candidates = [];
      let players = data['players']

      //ToDo: optimise - should be enouhg to do once
      //this.update_card_deck(data);
      //this.update_job_deck(data);

      for(let key in players){
        if(key == this.playerId){
          //populate current player hand
          
          let cards = players[key]['traits'];
          for(let tkey in cards){
            
            let found:Boolean = false;
            for(let k in this.cards){
              if(this.cards[k]['id'] == cards[tkey]['id']){
                found = true;  
              }
            }

            if(!found){
              this.cards.push(cards[tkey])
            }
          }
        }

        if(key != this.employer ) {
          //populate board with statuses
          let player = players[key]
          let ditems = []
          if(player.ready == false){
            ditems.push("Picking...");
          } else if(Object.keys(player.candidate_cards).length > 0) {
            for(let kid in player.candidate_cards){
              //these are ids, should resolve to cards
              let revealed = player.candidate_cards[kid];
              if(key == this.playerId || revealed){
                let actual_card = this.getTraitCardById(kid);
                if(actual_card){
                  ditems.push(actual_card['text']);
                } else {
                  ditems.push(kid);
                }
              }
            }
          } 
          
          if(ditems.length == 0) {
            ditems.push("Ready");
          }
          this.candidates.push({id:key, display_items:ditems});
        }
      }
    });
  }

  public interview(){

  }
  
  public declareWinner(){

  }

  public startTurn(){
      this.gamecomm.newTurn(this.gameId).subscribe((data)=>{
        console.log("START_TURN");
        console.log(data);

        this.candidates = [];
        this.employer = data['employer'];
        this.role = data['role']['text'];
        
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
    console.log("GAME_TICK");
    ctx.updateState();
    setTimeout(()=>{ctx.updatePeriodically(ctx)}, 2000);
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