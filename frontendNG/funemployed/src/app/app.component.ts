import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

export class AppComponent {
  title = 'funemployed';
  username = "gogu17";
  roomcode = "1234";

  constructor(private router: Router) {}

  hireMe() {
    this.router.navigate(['/gamelobby']);  // define your component where you want to go
  }
}
