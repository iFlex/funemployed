import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'rules-root',
    templateUrl: './rules.component.html',
    styleUrls: ['./rules.component.scss']
  })

export class RulesComponent implements OnInit {
    constructor(private router: Router) {}

    ngOnInit() {
    }
}