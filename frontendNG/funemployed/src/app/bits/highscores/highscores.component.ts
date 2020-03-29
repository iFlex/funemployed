import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'highscores-root',
    templateUrl: './highscores.component.html',
    styleUrls: ['./highscores.component.scss']
  })

export class HighscoresComponent implements OnInit {
    constructor(private router: Router) {}

    ngOnInit() {
    }
}