import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class GameLobbyService {

  constructor(private httpClient: HttpClient) {

  }

  public getGameLobbyStatus() {
      return this.httpClient.get("");
  }
}