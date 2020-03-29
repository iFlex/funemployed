import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { OfficeService } from './office.service';

class Cards {
  id: string
  description: string
  type: string
}

class Player {
  name: string
  hand: Cards[]
  jobs: string[]
}

@Component({
    selector: 'office-root',
    templateUrl: './office.component.html',
    styleUrls: ['./office.component.scss']
  })

export class OfficeComponent implements OnInit {
    players: Player[];
    constructor(private router: Router,
                private officeService: OfficeService) {

    }

    ngOnInit() {
      this.officeService.getOfficeStatus().subscribe((data)=>{
        console.log(data);
        this.players = data['players'];
      });
    }
}