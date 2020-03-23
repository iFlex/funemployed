
import random


class Deck:
	def __init__(self, cardlist):
		self.cards = cardlist


	def shuffle(self):
		random.shuffle(cards)


	def draw(self, count=1):
		if len(self.cards) < count:
			raise Exception("Draw attept is larger than the deck of cards")

		drawn = []
		for i in range(0, count):
			drawn.append(self.cards.pop(0))
		
		return drawn


	def add(self, cards):
		for card in cards:
			self.cards.append(card)

		self.shuffle()