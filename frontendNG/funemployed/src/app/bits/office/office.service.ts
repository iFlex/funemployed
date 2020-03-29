import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class OfficeService {

  constructor(private httpClient: HttpClient) {

  }

  public getOfficeStatus() {
      return this.httpClient.get("");
  }
}