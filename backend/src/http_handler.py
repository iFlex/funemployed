from gamefactory import GameFactory
from http.server import BaseHTTPRequestHandler
from http.cookies import SimpleCookie
from player import Player
import json
import base64
import mimetypes

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
GET  /[game_id]/interview-reveal/[player-id]/[card-id]       -> {"status":"ok"}
GET  /[game_id]/interview-end                                -> {"player_id": "test", "card_ids": ["selected_card_id_1","selected_card_id_2","selected_card_id_3"]}
GET  /[game_id]/turn-end/[hired-player-id]                   -> {"status":"success", "hired":"hired_player_id", "card":{"id":0, "card_text"}}
GET  /[game_id]/game-end                                     -> {"status":"ok"}  
GET  /[game_id]                                              -> {"status":"ok"}
'''

'''
    when user cookies are enabled - a base64 json encoded string of {userId:blalba}
'''

class RestHttpHandler(BaseHTTPRequestHandler):
    game_factory = GameFactory()
    serve_from = "./"
    encoding  = "utf-8"
    cookies_enabled = False

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


    
    def check_cookie(self):
        pass


    def encode_cookie(self, data):
        json = json.dumps(data)
        b64 = base64.b64encode(json)
        return ("Set-Cookie","cookie=%s; Domain=localhost;" % b64);

    def decode_cookie(self, raw_data):
        pass


    def handle_request(self, game_id, command, parameters, body):
        print("game_id:%s command:%s parameters:%s" %(game_id, command, parameters))
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
                    print(e)
                    return {"error":"failure","message":str(e)}

            if command == 'player-remove':
                if not (len(parameters) == 1):
                    return {"error":"invalid_parameter","message":"Please provide a player-id in the URL"}
                try:
                    game.remove_player(parameters[0])
                    return {'status':'ok'}
                except Exception as e:
                    print(e)
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
                    print(e)
                    return {"error":"failure","message":str(e)}

            if command == 'player-unready':
                try:
                    game.player_unready(parameters[0])
                    return {"status":"ok"}
                except Exception as e:
                    print(e)
                    return {"error":"failure","message":str(e)}

            if command == 'player-shuffle':
                return game.shuffle_player_order()

            if command == 'player-order':
                return game.get_player_order()

            if command == 'turn-start':
                try:
                    return game.start_turn()
                except Exception as e:
                    print(e)
                    return {"error":"failure","message":str(e)}

            if command == 'interview-start':
                if not (len(parameters) == 1):
                    return {"error":"invalid_parameter","message":"Please provide a player-id in the URL"}
                try:
                    return game.start_interview(parameters[0])
                except Exception as e:
                    print(e)
                    return {"error":"failure","message":str(e)}

            if command == 'interview-reveal':
                if not (len(parameters) == 2):
                    return {"error":"invalid_parameter","message":"Please provide a player-id and card-id in the URL"}
                try:
                    return game.reveal_card(parameters[0], parameters[1])
                except Exception as e:
                    print(e)
                    return {"error":"failure","message":str(e)}

            if command == 'interview-end':
                try:
                    return game.end_interview()
                except Exception as e:
                    print(e)
                    return {"error":"failure","message":str(e)}

            if command == 'turn-end':
                if not (len(parameters) == 1):
                    return {"error":"invalid_parameter","message":"Please provide the player-id or the hired candidate"}
                try:
                    return game.end_turn(parameters[0])
                except Exception as e:
                    print(e)
                    return {"error":"failure","message":str(e)}

            if command == 'game-end':
                try:
                    return game.end_game()
                except Exception as e:
                    print(e)
                    return {"error":"failure","message":str(e)}

            if command == None:
                try:
                    return game.to_json_dict()
                except Exception as e:
                    print(e)
                    return {"error":"failure","message":str(e)}

        return {"error":"invalid_request", "message":"request did not follow the correct URL convention"}


    def ensure_file_path_allowed(path):
        illegal_patterns = ['..']
        for pattern in illegal_patterns:
            if pattern in path:
                raise Eception("Illegal file path. Behave!")


    def path_to_mime_type(path):
        dot = path.rfind('.')
        ext = path[dot:]
        
        print("Excension:"+ext)

        return mimetypes.types_map[ext]


    def return_file(path):
        RestHttpHandler.ensure_file_path_allowed(path)

        try:
            f = open(RestHttpHandler.serve_from + path, "rb")
            return f.read()
        except Exception as e:
            print(e)
            return bytearray("kthxbay", RestHttpHandler.encoding)


    def do_GET(self):
        print(self.path)
        cookies = SimpleCookie(self.headers.get('Cookie'))
        print("Cookies:")
        print(cookies)

        api_pattern = "/api/";
        #very simplistic way of choosing when to server a file and when to server the API functions
        if api_pattern in self.path:
            indexof = self.path.index(api_pattern)
            game_id, command, parameters = RestHttpHandler.decode_url(self.path[indexof + len(api_pattern) - 1:])
            
            #if game_id doesn't match   - 
            #if player_id doesn't match -

            response_body = self.handle_request(game_id, command, parameters, None)

            status = 200

            if 'error' in response_body:
                status = 500
                print(response_body)

            self.send_response(status)
            self.send_header('Access-Control-Allow-Origin','*')
            self.send_header('Access-Control-Expose-Headers','Content-Type')
            self.send_header('Cache-Control','no-store, no-cache, must-revalidate')
            self.send_header('Content-Type','application/json;charset=%s' % RestHttpHandler.encoding)
            self.send_header('Response', status)
            self.end_headers()
            self.wfile.write(bytearray(json.dumps(response_body), RestHttpHandler.encoding)) 

        else:
            self.send_response(200)
            self.send_header('Content-type', RestHttpHandler.path_to_mime_type(self.path))
            self.end_headers()
            self.wfile.write(RestHttpHandler.return_file(self.path))


    def do_POST(self):
        self.send_response(200)
        self.send_header('Content-type','text/html')
        self.end_headers()
        self.wfile.write(bytearray("hello_post_world", RestHttpHandler.encoding)) #Doesnt work
        return