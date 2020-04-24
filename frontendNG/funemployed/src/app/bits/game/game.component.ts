import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { GameService } from './game.service';


@Component({
    selector: 'game-root',
    templateUrl: './game.component.html',
    styleUrls: ['./game.component.scss']
})

export class GameComponent implements OnInit {
    private selectedDivs:Object[]

    constructor(private router: Router,
                public game: GameService) { 
        this.selectedDivs = []              
    }

    ngOnInit(){

    }

    startTurn(event){
        this.game.startTurn();
    }
    
    candidateClick(event) {
        let id = event.target.id;
        if(this.game.allPlayersPresented()){
            this.game.declareWinner(id);
        } else {
            if(!this.game.isInterviewInProgress()){
                this.game.startInterview(id);
            } else {
                alert("Be patient... pls");
            }
        }
    }

    toggleReady(event){
        this.game.toggleReady(this.getSelectedCards());
    }

    getSelectedCards(){
        let selected = []
        for(let div in this.selectedDivs){
            selected.push(this.selectedDivs[div]['id']);
        }

        return selected;
    }

    revealCard(event){
        console.log("reveal_card");
        console.log(event);
        let id = event.target.id;
        console.log(id);
        this.game.revealCard(id);
    }

    selectionOn(target, className){
        target.classList.add(className);
    }
    
    selectionOff(target, className){
        target.classList.remove(className);
    }

    toggleCard(event){
        for(let i = 0; i < this.selectedDivs.length; ++i){
            let item = this.selectedDivs[i];

            if(item['id'] == event.target.id){
                this.selectionOff(event.target, "card-selected");
                this.selectedDivs.splice(i,1);
                console.log("Already in selection");
                return;
            }
        }
        
        if(this.selectedDivs.length == 3) {
            console.log("Cleaning up excess");
            let popped = this.selectedDivs.shift();
            this.selectionOff(popped, "card-selected");
        }

        this.selectedDivs.push(event.target);
        this.selectionOn(event.target, "card-selected");
    }
    
    leaveGame(event){
        this.game.leave();
    }

    refresh(event){
        this.game.updateState();
    }
}