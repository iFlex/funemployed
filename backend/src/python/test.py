from gamefactory import GameFactory
from player import Player
import json

factory = GameFactory()
game = factory.new_game()

print("new game %s" % game.get_id())

players = []
for i in range(0,4):
	players.append(Player(i,{}))

#add players	
for player in players:
	game.add_player(player)

for i in range(0,5):
	#start a turn
	tresult = game.start_turn()
	print("turn_start: %s" % json.dumps(tresult))
	
	#ready up players
	for player in players:
		cards = player.get_trait_cards()
		selection = []
		limit = 3
		for i in cards:
			selection.append(cards[i]['id'])
			limit -= 1
			if limit == 0:
				break
		r = game.player_ready(player.get_id(), selection)
		print("Ready up: %s" % json.dumps(r))

	winner_id = None
	#start interviewing
	for player in players:
		s = game.start_interview()
		print("interview_start: %s" % json.dumps(s))
		
		if 'error' not in s and winner_id == None:
			winner_id = s['player_id']

		e = game.end_interview()
		print("interview_end: %s" % json.dumps(e))

	et = game.end_turn(winner_id)
	print("turn_end: %s" % json.dumps(et))

factory.end_game(game.get_id())