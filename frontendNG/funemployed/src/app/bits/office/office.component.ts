import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'office-root',
    templateUrl: './office.component.html',
    styleUrls: ['./office.component.scss']
  })

export class OfficeComponent implements OnInit {
    constructor(private router: Router) {}

    ngOnInit() {
    }
}