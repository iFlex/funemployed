from gamefactory import GameFactory
from http.server import BaseHTTPRequestHandler
from player import Player
import json

'''
GET  /game-new                                               -> {"game_id": "UYJCR"}  
GET  /[game_id]/player-add/[user_id]                         -> {"status": "ok"}
GET  /[game_id]/player-remove/[user_id]                      -> {'status':'ok'}
GET  /[game_id]/players-shuffle                              -> ["user_id_1", "user_id_2", "user_id_2"]
GET  /[game_id]/player-order                                 -> ["user_id_1", "user_id_2", "user_id_2"]
GET  /[game_id]/turn-start                                   -> {"role": {"id": 0, "text": "card_content"}, "employer": "employer_id", "candidates": [{"player": "test", "new_cards": [{"id": 0, "text": "a"}, {"id": 1, "text": "b"}, {"id": 2, "text": "c"}, {"id": 3, "text": "d"}, {"id": 4, "text": "e"}, {"id": 5, "text": "f"}]}, {"player": "test2", "new_cards": [{"id": 6, "text": "g"}, {"id": 7, "text": "h"}, {"id": 8, "text": "i"}, {"id": 9, "text": "j"}, {"id": 10, "text": "k"}, {"id": 11, "text": "l"}]}, {"player": "test3", "new_cards": [{"id": 12, "text": "m"}, {"id": 13, "text": "n"}, {"id": 14, "text": "o"}, {"id": 15, "text": "p"}, {"id": 16, "text": "q"}, {"id": 17, "text": "r"}]}]}
GET  /[game-id]/player-ready/card_id_1/card_id_2/card_id_3   -> {"status": "ok"}
POST /[game_id]/player-ready                                
    BODY:      [card_id_1, card_id_2, card_id_3] 
GET  /[game_id]/player-unready                               -> {"status": "ok"}
GET  /[game_id]/interview-start/[player-id]                  -> {"player_id": "test", "card_ids": ["selected_card_id_1","selected_card_id_2","selected_card_id_3"]}
GET  /[game_id]/interview-end                                -> {"player_id": "test", "card_ids": ["selected_card_id_1","selected_card_id_2","selected_card_id_3"]}
GET  /[game_id]/turn-end/[hired-player-id]                   -> {"status":"success", "hired":"hired_player_id", "card":{"id":0, "card_text"}}
GET  /[game_id]/game-end                                     -> {"status":"ok"}  
GET  /[game_id]                                              -> {"status":"ok"}
'''

class RestHttpHandler(BaseHTTPRequestHandler):
    game_factory = GameFactory()

        
    def decode_url(url):
        components = url.split('/')
        game_id = components[1]
        command = None
        if len(components) > 2:
            command = components[2]
        parameters = None
        if len(components) > 3:
            parameters = components[3:]
        
        return (game_id, command, parameters)


    def handle_request(self, game_id, command, parameters, body):
        print("game_id:'%s' command:'%s'" % (game_id, command))
        if game_id == 'game-new':
            game = RestHttpHandler.game_factory.new_game()
            return {'game_id':game.get_id()}
        else:
            game = RestHttpHandler.game_factory.get_game(game_id)
            if game == None:
                return {"error":"invalid_game_id","message":"This game doesn't exist"}

            if command == 'player-add':
                if not (len(parameters) == 1):
                    return {"error":"invalid_parameter","message":"Please provide a player-id in the URL"}
                try:
                    return game.add_player(Player(parameters[0], {}))
                except Exception as e:
                    return {"error":"failure","message":str(e)}

            if command == 'player-remove':
                if not (len(parameters) == 1):
                    return {"error":"invalid_parameter","message":"Please provide a player-id in the URL"}
                try:
                    game.remove_player(parameters[0])
                    return {'status':'ok'}
                except Exception as e:
                    return {"error":"failure","message":str(e)}
            
            if command == 'player-ready':
                if len(parameters) < 1:
                    return {"error":"invalid_parameter","message":"Please provide a player-id in the URL"}
                try:
                    if len(parameters) > 1 and body == None:
                        print("Using get version of this request")
                        body_str = parameters[1:]
                        body = []
                        for sitm in body_str:
                            body.append(int(sitm))
                        print("body:")
                        print(body)
                    return game.player_ready(parameters[0], body)
                except Exception as e:
                    return {"error":"failure","message":str(e)}

            if command == 'player-unready':
                try:
                    game.player_unready(parameters[0])
                    return {"status":"ok"}
                except Exception as e:
                    return {"error":"failure","message":str(e)}

            if command == 'player-shuffle':
                return game.shuffle_player_order()

            if command == 'player-order':
                return game.get_player_order()

            if command == 'turn-start':
                try:
                    return game.start_turn()
                except Exception as e:
                    return {"error":"failure","message":str(e)}

            if command == 'interview-start':
                if not (len(parameters) == 1):
                    return {"error":"invalid_parameter","message":"Please provide a player-id in the URL"}
                try:
                    return game.start_interview(parameters[0])
                except Exception as e:
                    return {"error":"failure","message":str(e)}

            if command == 'interview-end':
                try:
                    return game.end_interview()
                except Exception as e:
                    return {"error":"failure","message":str(e)}

            if command == 'turn-end':
                if not (len(parameters) == 1):
                    return {"error":"invalid_parameter","message":"Please provide the player-id or the hired candidate"}
                try:
                    return game.end_turn(parameters[0])
                except Exception as e:
                    return {"error":"failure","message":str(e)}

            if command == 'game-end':
                try:
                    return game.end_game()
                except Exception as e:
                    return {"error":"failure","message":str(e)}

            if command == None:
                try:
                    return game.to_json_dict()
                except Exception as e:
                    return {"error":"failure","message":str(e)}

        return {"error":"invalid_request", "message":"request did not follow the correct URL convention"}


    def do_GET(self):
        print(self.path)

        game_id, command, parameters = RestHttpHandler.decode_url(self.path)
        response_body = self.handle_request(game_id, command, parameters, None)

        status = 200
        if 'error' in response_body:
            status = 500

        self.send_response(status)
        self.send_header('Content-type','application/json')
        self.end_headers()
        self.wfile.write(bytearray(json.dumps(response_body), "utf-8")) #Doesnt work
        return


    def do_POST(self):
        self.send_response(200)
        self.send_header('Content-type','text/html')
        self.end_headers()
        self.wfile.write(bytearray("hello_post_world","utf-8")) #Doesnt work
        return