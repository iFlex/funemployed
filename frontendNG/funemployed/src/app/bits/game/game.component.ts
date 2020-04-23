import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { GameService } from './game.service';
import { GameCommService } from '../../services/gamecomm.service';


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

    selectionOn(target){
        target.style.backgroundColor = "blue";
    }
    
    selectionOff(target){
        target.style.backgroundColor = "white";
    }

    toggleCard(event){
        for(let i = 0; i < this.selectedDivs.length; ++i){
            let item = this.selectedDivs[i];

            if(item['id'] == event.target.id){
                this.selectionOff(event.target);
                this.selectedDivs.splice(i,1);
                console.log("Already in selection");
                return;
            }
        }
        
        if(this.selectedDivs.length == 3) {
            console.log("Cleaning up excess");
            let popped = this.selectedDivs.shift();
            this.selectionOff(popped);
        }

        this.selectedDivs.push(event.target);
        this.selectionOn(event.target);
    }
    
    refresh(event){
        this.game.updateState();
    }
}