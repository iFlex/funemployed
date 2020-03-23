class Player
	def __init__(self, id, details):
		self.id = id
		self.details = details
		self.traits = {}
		self.won = {}


	def getId():
		return self.id


	def addTraitCards(self, cards):
		for card in cards:
			id = card['id']
			if id in self.traits:
				raise Exception("Duplicate card added to player hand")
			self.traits[id] = card


	def takeCards(self, card_ids):
		for card_id in card_ids:
			if card_id not in self.traits:
				raise Exception("Attempted to card from player hand that does not exist in player hand")
			del self.traits[card_id]


	def addWonJobCard(self, card):
		id = card['id']
		if id in self.won:
			raise Exception("Duplicate won card added to player hand")

		self.won[id] = card