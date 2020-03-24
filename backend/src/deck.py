import random


class Deck:
	def __init__(self, cardlist):
		self.cards = []
		
		i = 0
		for card in cardlist:
			self.cards.append({'id':i,'text':card})
			i += 1


	def shuffle(self):
		random.shuffle(cards)


	def size(self):
		return len(self.cards)


	def draw(self, count=1):
		if len(self.cards) < count:
			raise Exception("Draw attept is larger than the deck of cards")

		drawn = self.cards[0:count]
		self.cards = self.cards[count:]
		return drawn


	def add(self, cards):
		for card in cards:
			self.cards.append(card)

		self.shuffle()


	def to_json_dict(self):
		return self.cards