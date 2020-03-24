import React, {Component} from 'react'
import logo from './resources/unnamed.png'
import { render } from 'react-dom'
import { BrowserRouter, Route, Switch, NavLink } from 'react-router-dom';
import { Form, Button, FormGroup, FormControl, ControlLabel } from "react-bootstrap";
import './App.css'
import Popup from "reactjs-popup"
import Rulez from './component/Rulez'
import Office from './component/Office'


class App extends Component {
  constructor(props) {
    super(props)
    this.state = {
      username : 'gogu17',
      roomcode : '1234'
    }
  }
  
  componentWillMount() {
  }
  
  handleUser(e) {
  }

  handleRoom(e) {
  }

  login(props) {
    console.log('Link was clicked')
    console.log(props)
    props.router.push({
      pathname: '/office',
      state: {
        username: this.state.username,
        //roomcode: this.state.roomcode,
        score: 0,
        cards: []
      }
    })
  }

  render() {   
    return (
      <header>
        <img src={logo} />
        <Form>            
            <input type="text" name="username" defaultValue={this.state.username} onChange={this.handleUser}/>
            <input type="text" name="roomcode" defaultValue={this.state.roomcode} onChange={this.handleRoom}/>

            <Button variant="secondary" type="submit" onClick={this.login}>
              Get Hired
            </Button>

            <Popup trigger={<Button> Trigger</Button>} position="right center">
              Rulez and shit
            </Popup>
        </Form>
      </header>
    )
  }
}

export default App;
