import random
import string


class Player:
	def __init__(self, id, details):
		if 'token' in details:
			self.token = details['token']
		else:
			self.token = ''.join(random.choice(string.ascii_letters) for x in range(128)).upper()
		
		self.id = id
		self.details = details
		self.traits = {}
		self.won = {}
		self.candidate_cards = {}
		self.ready = False


	def get_token(self):
		return self.token


	def equals(self, other):
		return self.id == other.get_id() and self.token == other.get_token()


	def get_id(self):
		return self.id


	def is_ready(self):
		return self.ready


	def set_ready(self, ready):
		self.ready = ready


	def drop_candidate_cards(self):
		self.candidate_cards = {}


	def candidate_cards_to_hand(self):
		for cid in self.candidate_cards:
			self.traits[cid] = {"id":card_id, "text":text}

		self.drop_candidate_cards()


	def all_cards_revealed(self):
		for key in self.candidate_cards:
			if self.candidate_cards[key]['revealed'] != True:
				return False
		return True
	

	def set_candidate_cards(self, card_ids):
		for card_id in card_ids:
			if card_id not in self.traits:
				print(self.traits)
				print(card_id)
				raise Exception("Player attempted to select card they don't own. Stop stealing!!!")
		
		for card_id in card_ids:
			text = "None"
			if card_id in self.traits:
				text = self.traits[card_id]['text']

			self.candidate_cards[card_id] = {"id":card_id, "text":text, "revealed":False}


	def reveal_card(self, card_id):
		card_id = int(card_id)
		if card_id not in self.candidate_cards:
			print(card_id)
			print(self.candidate_cards)
			raise Exception("Attempted to reveal card outside of selection of cards")
		self.candidate_cards[card_id]['revealed'] = True


	def get_candidate_cards(self):
		return self.candidate_cards


	def get_trait_cards(self):
		return self.traits


	def get_won_cards(self):
		return self.won


	def add_trait_cards(self, cards):
		for card in cards: 
			id = card['id']
			if id in self.traits:
				raise Exception("Duplicate card added to player hand")
			self.traits[id] = card


	def drop_trait_cards(self, card_ids):
		for card_id in card_ids:
			if card_id not in self.traits:
				raise Exception("Attempted to drop card from player hand that does not exist in player hand")
			del self.traits[card_id]


	def add_won_job_card(self, card):
		id = card['id']
		if id in self.won:
			raise Exception("Duplicate won card added to player hand")

		self.won[id] = card


	def to_json_dict(self):
		result = {
			'id':self.id,
			'candidateCards':self.candidate_cards,
			'ready':self.ready,
			'traits':[],
			'wonCards':[]
		}

		for trait in self.traits:
		 	result['traits'].append(self.traits[trait])

		for won_c in self.won:
		 	result['wonCards'].append(self.won[won_c])
		
		return result