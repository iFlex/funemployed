import random


class Game:
	MAX_PLAYER_HAND = 6
	MAX_INTERVIEW_CARD_COUNT = 3

	def __init__(self, id, jobs_deck, traits_deck):
		self.id = id
		self.jobs= jobs_deck
		self.traits = traits_deck
		self.players = {}
		self.player_order = []
		self.players_interviewed = {}
		
		self.turn_in_progress      = False
		self.interview_in_progress = False
		
		self.current_employer        = None
		self.current_interviewer_pos = None
		self.current_candidate       = None
		self.current_role            = None

		self.ready_players_count = 0


	def get_id(self):
		return self.id


	def pick_next_candidate(self):
		for player_id in self.player_order:
			if not (self.current_employer.get_id() == player_id) and (player_id not in self.players_interviewed):
				self.current_candidate = self.players[player_id]


	def pick_next_employer(self):
		if self.current_interviewer_pos == None:
			self.current_interviewer_pos = 0 #random please

		self.current_interviewer_pos = (self.current_interviewer_pos + 1) % len(self.player_order)
		self.current_employer = self.players[self.player_order[self.current_interviewer_pos]] 


	def retrieve_player(self, player_id):
		player = self.players.get(player_id, None)
		if player == None:
			raise Exception("Invalid player id %s" % player_id)
		return player


	def pick_next_role(self):
		if self.jobs.size() > 0:
			return self.jobs.draw(1)[0]
		return None


	def add_player(self, player):	
		id = player.get_id()
		if id in self.players:
			raise Exception("Attempted to add duplicate player record")

		self.players[id] = player
		self.player_order.append(id)


	#ToDo: handle exit of interviewer, current candidate, interviewed candidate, not interviewed candidate
	def remove_player(self, player_id):
		if player_id not in self.players:
			raise Exception("Attempted to remove inexistent player")

		if self.players[player_id].is_ready() == True:
			self.ready_players_count -= 1

		del self.players[player_id]
		for i in range(0, self.player_order):
			if self.player_order[i] == player_id:
				self.player_order.pop(i)
				break


	def shuffle_player_order(self):
		random.shuffle(self.player_order)
		return self.player_order

	def get_player_order(self):
		return self.player_order


	def replenish_player_cards(self, player):
		needed_count = Game.MAX_PLAYER_HAND
		count = len(player.get_trait_cards())
		needed_count -= count

		new_cards = self.traits.draw(needed_count)
		player.add_trait_cards(new_cards)
		return new_cards
	

	def replenish_cards(self):
		candidate_cards = []
		for player_id in self.players:
			current_player = self.players[player_id]
			cards = self.replenish_player_cards(current_player)
			candidate_cards.append({
				"player":current_player.get_id(),
				"new_cards":cards
			})
		return candidate_cards


	def start_turn(self):
		if self.turn_in_progress == True or self.interview_in_progress == True:
			raise Exception("Attempted to start a new turn while a turn or interview is in progress")

		self.pick_next_employer()
		self.current_role     = self.pick_next_role()

		if self.current_role == None:
			#come on, you've played throught all of the roles, aren't you bored already?>
			return {"error":"game_over","message":"All roles played"}

		if len(self.players) < 3:
			#can't really play with such a smol number of peeps
			return {"error":"insufficient_players","message":"Not enought players left"}

		#replenish cards
		try:
			new_cards = self.replenish_cards()
		except Exception as e:
			#ToDo: use custom exception
			return {"error":"deck_draw_fail","message":"Draw deck is too small"}

		self.players_interviewed = {}
		self.ready_players_count = 0
		self.turn_in_progress = True
		
		return {"role":self.current_role, "employer":self.current_employer.get_id(), "candidates":new_cards}


	def player_unready(self, player_id):
		player = self.retrieve_player(player_id)
		player.clear_candidate_cards()
		
		if player.is_ready() == True:
			self.ready_players_count -= 1
			player.set_ready(False)


	def player_ready(self, player_id, card_ids):
		player = self.retrieve_player(player_id)
		if player.equals(self.current_employer):
			return {"error":"invalid_request","message":"employer does not need to ready up and submit cards"}
		if not (len(card_ids) == Game.MAX_INTERVIEW_CARD_COUNT):
			return {"error":"invalid_request","message":"Player has selected %d cards for an interview, they should have chosen %d" % (Game.MAX_INTERVIEW_CARD_COUNT, len(card_ids))}

		player.set_candidate_cards(card_ids)
		if player.is_ready() == False:
			self.ready_players_count += 1
			player.set_ready(True)

		return {"status":"ok"}


	def all_candidates_ready(self):
		needed_count = len(self.players) - 1
		return self.ready_players_count == needed_count


	def all_candidates_interviewed(self):
		needed_count = len(self.players) - 1
		return len(self.players_interviewed) == needed_count


	def start_interview(self, player_id=None):
		if self.interview_in_progress == True:
			return {"error":"invalid_state","message":"An interview is already in progress, please close it to start a new one"}
		if not self.turn_in_progress:
			return {"error":"invalid_state","message":"No turn is in progress, cannot start interview"}
		if not (player_id == self.current_employer.get_id()):
			return {"error":"invalid_request","message":"Cannot interview the employer..."} 
		if not self.all_candidates_ready():
			return {"error":"invalid_state","message":"not all candidates are ready for interviewing"}
		if self.all_candidates_interviewed():
			return {"error":"invalid_state","message":"All candidates were interviewed, can't start another interview"}
		
		if player_id == None:
			self.pick_next_candidate()
		else:
			self.current_candidate = self.retrieve_player(player_id)
		
		self.interview_in_progress = True
		self.current_candidate.set_ready(False)
		
		used_cards = self.current_candidate.get_candidate_cards()
		self.current_candidate.drop_trait_cards(used_cards)
		#ToDo: implement discard pile

		return {"player_id":self.current_candidate.get_id(), "card_ids":self.current_candidate.get_candidate_cards()}


	def end_interview(self):
		if self.interview_in_progress == True:
			self.interview_in_progress = False
			self.players_interviewed[self.current_candidate.get_id()] = True
			return {"player_id":self.current_candidate.get_id(), "card_ids":self.current_candidate.get_candidate_cards()}

		return {"error":"invalid_state","message":"No interview in progress"}


	def end_turn(self, hired_player_id):
		if self.interview_in_progress == True:
			return {"error":"invalid_state","message":"Interview is still in progress, please end it and then end the turn"}
		if not self.all_candidates_interviewed():
			return {"error":"invalid_state","message":"Not all candidates have been intervewed. Either remove other players or interview them"}
		if self.turn_in_progress == True:
			hired = self.retrieve_player(hired_player_id)
			hired.add_won_job_card(self.current_role)

			self.turn_in_progress = False
			return {"status":"success", "hired":hired_player_id, "card":self.current_role}

		return {"error":"invalid_request","message":"No turn in progress, cannot end an inexistent turn"}


	def end_game(self):
		return {"status":"success"}


	def to_json_dict(self):
		result = {
			'id':self.id,
			'player_order':self.player_order,
			'turn_in_progress':self.turn_in_progress,
			'interview_in_progress':self.interview_in_progress,
			'ready_players_count':self.ready_players_count,
			'current_interviewer_pos':self.current_interviewer_pos,
			'current_role':self.current_role,
			'players':{},
			"players_interviewed":{}
		}

		if self.current_employer:
			result['current_employer'] = self.current_employer.to_json_dict()
		if self.current_candidate:
			result['current_candidate'] = self.current_candidate.to_json_dict()
		if self.jobs:
			result['jobs'] = self.jobs.to_json_dict()
		if self.traits:
			result['traits'] = self.traits.to_json_dict()

		for player in self.players:
			result['players'][player] = self.players[player].to_json_dict()

		for player in self.players_interviewed:
			result['players_interviewed'][player] = True

		print(result)
		return result
		