import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'gamelobby-root',
    templateUrl: './gamelobby.component.html',
    styleUrls: ['./gamelobby.component.scss']
  })

export class GameLobbyComponent implements OnInit {
    constructor(private router: Router) {}

    ngOnInit() {
    }
}