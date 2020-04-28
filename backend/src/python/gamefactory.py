from deck import Deck
from game import Game
import json
import random
import string 

class GameFactory:
	def __init__(self):
		self.PATH_TO_DECK = "C:\\Users\\gamer\\Documents\\GitHub\\funemployed\\backend\\resources\\card_packs\\ro"
		self.registry = {}


	def generate_game_id(self, count=5):
		return ''.join(random.choice(string.ascii_letters) for x in range(count)).upper()


	def decks_from_json_file(self, path):
		with open(self.PATH_TO_DECK+"\\jobs") as f:
			lines = f.readlines()
		jobs = Deck(lines)

		with open(self.PATH_TO_DECK+"\\traits") as f:
			lines = f.readlines()
		traits = Deck(lines)

		return(jobs, traits)


	def new_game(self):
		id = self.generate_game_id()
		if id in self.registry:
			raise Exception("Game with this id already exists")
		jobs, traits = self.decks_from_json_file(self.PATH_TO_DECK)
		
		#shuffle decks before starting
		jobs.shuffle()
		traits.shuffle()

		self.registry[id] = Game(id, jobs, traits)
		return self.registry[id]


	def get_game(self, game_id):
		return self.registry.get(game_id, None)


	def end_game(self, game_id):
		if game_id not in self.registry:
			raise Exception("Attempt to close inexistent game")

		#todo: other closure stuff here
		self.registry[game_id].end_game()
		del self.registry[game_id]