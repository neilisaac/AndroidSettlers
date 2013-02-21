package com.settlers.hd;

import java.util.Vector;

import android.content.Context;

import com.settlers.hd.Board.Cards;
import com.settlers.hd.Hexagon.Type;

public class Player {

	private static boolean FREE_BUILD = false;
	private static boolean mixedTrade = false;

	public static final int MAX_TOWNS = 5;
	public static final int MAX_CITIES = 4;
	public static final int MAX_ROADS = 15;

	public static final int[] ROAD_COST = { 1, 0, 0, 1, 0 };
	public static final int[] TOWN_COST = { 1, 1, 1, 1, 0 };
	public static final int[] CITY_COST = { 0, 0, 2, 0, 3 };
	public static final int[] CARD_COST = { 0, 1, 1, 0, 1 };

	private Color color;
	private String name;
	protected int towns;
	protected int cities;
	private int soldiers, victory, tradeValue, roadLength;
	private int[] resources, cards;
	private boolean[] traders;
	private Vector<Cards> newCards;
	private boolean usedCard;
	private int index, type;
	private String actionLog;
	private Vertex lastTown;

	protected Vector<Vertex> settlements, reaching;
	protected Vector<Edge> roads;

	protected Board board;

	public enum Color {
		RED, BLUE, GREEN, ORANGE, SELECT, NONE
	}

	public static final int PLAYER_HUMAN = 0;
	public static final int PLAYER_BOT = 1;
	public static final int PLAYER_ONLINE = 2;

	/**
	 * Initialize player object
	 * 
	 * @param board
	 *            board reference
	 * @param number
	 *            the player number
	 * @param name
	 *            player name
	 * @param type
	 *            PLAYER_HUMAN, PLAYER_BOT, or PLAYER_ONLINE
	 */
	public Player(Board board, int index, Color color, String name, int type) {
		this.board = board;
		this.color = color;
		this.name = name;
		this.type = type;
		this.index = index;

		towns = 0;
		cities = 0;
		soldiers = 0;
		roadLength = 0;
		victory = 0;
		tradeValue = 4;
		usedCard = false;
		actionLog = "";
		lastTown = null;

		newCards = new Vector<Cards>();

		settlements = new Vector<Vertex>();
		reaching = new Vector<Vertex>();
		roads = new Vector<Edge>();

		// initialise number of each kind of development card
		cards = new int[Board.Cards.values().length];
		for (int i = 0; i < cards.length; i++)
			cards[i] = 0;

		resources = new int[Hexagon.TYPES.length];
		traders = new boolean[Type.values().length];
		for (int i = 0; i < resources.length; i++) {
			resources[i] = 0;
			traders[i] = false;
		}
	}

	/**
	 * Roll the dice
	 * 
	 * @return the result of the roll
	 */
	public int roll() {
		return roll((int) (Math.random() * 6) + (int) (Math.random() * 6) + 2);
	}

	/**
	 * Roll the dice with a predefined result
	 * 
	 * @param roll
	 *            the desired roll
	 * @return the result of the roll
	 */
	public int roll(int roll) {
		appendAction(R.string.player_roll, Integer.toString(roll));
		board.roll(roll);

		return roll;
	}

	/**
	 * Function called at the beginning of the turn
	 */
	public void beginTurn() {
		// clear the action log
		actionLog = "";
	}

	/**
	 * Function called at the end of the build phase
	 */
	public void endTurn() {
		// add new cards to the set of usable cards
		for (int i = 0; i < newCards.size(); i++)
			cards[newCards.get(i).ordinal()] += 1;

		newCards.clear();
		usedCard = false;

		appendAction(R.string.player_ended_turn);
	}

	/**
	 * Attempt to build a road on edge. Returns true on success
	 * 
	 * @param edge
	 *            edge to build on
	 * @param setup
	 *            setup phase (Y/N?)
	 * @return
	 */
	public boolean build(Edge edge) {
		if (edge == null || !canBuild(edge))
			return false;

		// check resources
		boolean free = board.isSetupPhase() || board.isProgressPhase();
		if (!free && !affordRoad())
			return false;

		if (!edge.build(this))
			return false;

		if (!free) {
			useResources(Type.BRICK, 1);
			useResources(Type.LUMBER, 1);
		}

		appendAction(R.string.player_road);

		boolean hadLongest = (board.getLongestRoadOwner() == this);
		board.checkLongestRoad();

		if (!hadLongest && board.getLongestRoadOwner() == this)
			appendAction(R.string.player_longest_road);

		roads.add(edge);

		Vertex vertex = edge.getVertex1();
		if (!reaching.contains(vertex))
			reaching.add(vertex);

		vertex = edge.getVertex2();
		if (!reaching.contains(vertex))
			reaching.add(vertex);

		return true;
	}

	/**
	 * Attempt to build an establishment on vertex. Returns true on success
	 * 
	 * @param vertex
	 *            vertex to build on
	 * @param setup
	 *            setup phase (Y/N?)
	 * @return
	 */
	public boolean build(Vertex vertex, int type) {
		if (vertex == null || !canBuild(vertex, type))
			return false;

		boolean setup = board.isSetupPhase();

		// check resources based on type we want to build
		if (type == Vertex.TOWN) {
			if (!setup && !affordTown())
				return false;
		} else if (type == Vertex.CITY) {
			if (!setup && !affordCity())
				return false;
		} else {
			// invalid type
			return false;
		}

		if (!vertex.build(this, type, setup))
			return false;

		// deduct resources based on type
		if (vertex.getBuilding() == Vertex.TOWN) {
			if (!setup) {
				useResources(Type.BRICK, 1);
				useResources(Type.LUMBER, 1);
				useResources(Type.GRAIN, 1);
				useResources(Type.WOOL, 1);
			}
			towns += 1;
			settlements.add(vertex);
			board.checkLongestRoad();
		} else {
			if (!setup) {
				useResources(Type.GRAIN, 2);
				useResources(Type.ORE, 3);
			}
			towns -= 1;
			cities += 1;
		}

		// collect resources for second town during setup
		if (board.isSetupPhase2()) {
			for (int i = 0; i < 3; i++) {
				Hexagon hexagon = vertex.getHexagon(i);
				if (hexagon != null && hexagon.getType() != Type.DESERT)
					addResources(hexagon.getType(), 1);
			}
		}

		lastTown = vertex;

		appendAction(type == Vertex.TOWN ? R.string.player_town
				: R.string.player_city);

		return true;
	}

	/**
	 * Can you build on this edge? Maybe
	 * 
	 * @param edge
	 * @return
	 */
	public boolean canBuild(Edge edge) {
		if (edge == null || roads.size() >= MAX_ROADS)
			return false;

		if (board.isSetupPhase()) {
			// check if the edge is adjacent to the last town built
			if (lastTown != edge.getVertex1() && lastTown != edge.getVertex2())
				return false;
		}

		return edge.canBuild(this);
	}

	/**
	 * Can you build on this vertex? We'll see
	 * 
	 * @param vertex
	 * @param setup
	 * @return
	 */
	public boolean canBuild(Vertex vertex, int type) {
		if (type == Vertex.TOWN && towns >= MAX_TOWNS)
			return false;
		else if (type == Vertex.CITY && cities >= MAX_CITIES)
			return false;

		return vertex.canBuild(this, type, board.isSetupPhase());
	}

	/**
	 * Returns the number of cards in the players hand
	 * 
	 * @return
	 */
	public int getResourceCount() {
		int sum = 0;
		for (int i = 0; i < resources.length; i++)
			sum += resources[i];
		return sum;
	}

	/**
	 * Add resources to the player
	 * 
	 * @param type
	 *            type of resources to add
	 * @param count
	 *            number of that resource to add
	 */
	public void addResources(Type type, int count) {
		resources[type.ordinal()] += count;
	}

	/**
	 * Get the number of resources a player has of a given type
	 * 
	 * @param type
	 *            the type
	 * @return the number of resources
	 */
	public int getResources(Type type) {
		return resources[type.ordinal()];
	}

	/**
	 * Get a copy of the player's resource list
	 * 
	 * @return an editable copy of the player's resource list
	 */
	public int[] getResources() {
		int[] list = new int[resources.length];
		for (int i = 0; i < resources.length; i++)
			list[i] = resources[i];

		return list;
	}

	/**
	 * Consume resources of a given type
	 * 
	 * @param type
	 *            the type to use
	 * @param count
	 *            the number to use
	 */
	public void useResources(Type type, int count) {
		resources[type.ordinal()] -= count;
	}

	/**
	 * Pick a random resource and deduct it from this player
	 * 
	 * @return the type stolen
	 */
	private Type stealResource() {
		int count = getResourceCount();
		if (count <= 0)
			return null;

		// pick random card
		int select = (int) (Math.random() * count);
		for (int i = 0; i < resources.length; i++) {
			if (select < resources[i]) {
				useResources(Type.values()[i], 1);
				return Type.values()[i];
			}

			select -= resources[i];
		}

		return null;
	}

	/**
	 * Steal a resource from another player
	 * 
	 * @param from
	 *            the player to steal from
	 * @return the type of resource stolen
	 */
	public Type steal(Player from) {
		Type type = from.stealResource();
		return steal(from, type);
	}

	/**
	 * Steal a resource from another player
	 * 
	 * @param from
	 *            the player to steal from
	 * @param type
	 *            the type of card to be stolen
	 * @return the type of resource stolen
	 */
	public Type steal(Player from, Type type) {
		if (type != null) {
			addResources(type, 1);
			appendAction(R.string.player_stole_from, from.getName());
		}
		
		return type;
	}

	/**
	 * Discard one resource of a given type
	 * 
	 * @param type
	 *            or null for random
	 */
	public void discard(Type type) {
		Type choice = type;

		// pick random type if none is specified
		if (choice == null) {
			while (true) {
				int pick = (int) (Math.random() * Hexagon.TYPES.length);
				if (resources[pick] > 0) {
					choice = Hexagon.TYPES[pick];
					break;
				}
			}
		}

		useResources(choice, 1);

		int res = Hexagon.getTypeStringResource(choice);
		appendAction(R.string.player_discarded, res);
	}

	/**
	 * Trade with another player
	 * 
	 * @param player
	 *            the player to trade with
	 * @param type
	 *            type of resource to trade for
	 * @param trade
	 *            the resoruces to give the player
	 */
	public void trade(Player player, Type type, int[] trade) {
		addResources(type, 1);
		player.useResources(type, 1);

		for (int i = 0; i < Hexagon.TYPES.length; i++) {
			if (trade[i] <= 0)
				continue;

			useResources(Hexagon.TYPES[i], trade[i]);
			player.addResources(Hexagon.TYPES[i], trade[i]);

			for (int j = 0; j < trade[i]; j++) {
				appendAction(R.string.player_traded_away, Hexagon
						.getTypeStringResource(Hexagon.TYPES[i]));
			}
		}

		appendAction(R.string.player_traded_with, player.getName());
		appendAction(R.string.player_got_resource, Hexagon
				.getTypeStringResource(type));
	}

	/**
	 * Get the player's Color
	 * 
	 * @return the player's Color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Get the player's index number
	 * 
	 * @return the index number [0, 3]
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Determine if the player can build a road
	 * 
	 * @return true if the player can build a road
	 */
	public boolean affordRoad() {
		return (FREE_BUILD || roads.size() < MAX_ROADS
				&& getResources(Type.BRICK) >= 1
				&& getResources(Type.LUMBER) >= 1);
	}

	/**
	 * Determine if a player can build a town
	 * 
	 * @return true if the player can build a town
	 */
	public boolean affordTown() {
		return (FREE_BUILD || towns < MAX_TOWNS
				&& getResources(Type.BRICK) >= 1
				&& getResources(Type.LUMBER) >= 1
				&& getResources(Type.GRAIN) >= 1
				&& getResources(Type.WOOL) >= 1);
	}

	/**
	 * Determine if the player can build a city
	 * 
	 * @return true if the player can build a city
	 */
	public boolean affordCity() {
		return (FREE_BUILD || cities < MAX_CITIES
				&& getResources(Type.GRAIN) >= 2 && getResources(Type.ORE) >= 3);
	}

	/**
	 * Determine if the player can buy a card
	 * 
	 * @return true if the player can buy a card
	 */
	public boolean affordCard() {
		return (FREE_BUILD || getResources(Type.WOOL) >= 1
				&& getResources(Type.GRAIN) >= 1 && getResources(Type.ORE) >= 1);
	}

	/**
	 * Get the number of victory points that are evident to other players
	 * 
	 * @return the number of victory points
	 */
	public int getPublicVictoryPoints() {
		int points = towns + 2 * cities;

		if (board.hasLongestRoad(this))
			points += 2;

		if (board.hasLargestArmy(this))
			points += 2;

		return points;
	}

	/**
	 * Return player's current total victory points
	 * 
	 * @return the number of victory points
	 */
	public int getVictoryPoints() {
		return getPublicVictoryPoints() + victory;
	}

	/**
	 * Buy a card
	 * 
	 * @return the card type
	 */
	public Board.Cards buyCard() {
		return buyCard(board.getDevelopmentCard());
	}

	/**
	 * Buy a predetermined card
	 * 
	 * @param card
	 *            the type of card to buy
	 * @return the type of card bought
	 */
	public Board.Cards buyCard(Board.Cards card) {
		if (!affordCard())
			return null;

		// out of cards
		if (card == null)
			return null;

		// deduct resources
		useResources(Type.WOOL, 1);
		useResources(Type.GRAIN, 1);
		useResources(Type.ORE, 1);

		if (card == Cards.VICTORY)
			victory += 1;
		else
			newCards.add(card);

		appendAction(R.string.player_bought_card);

		return card;
	}

	/**
	 * Get the player's development cards
	 * 
	 * @return an array with the number of each type of card
	 */
	public int[] getCards() {
		return cards;
	}

	/**
	 * Get the number of a given development card type that a player has
	 * 
	 * @param card
	 *            the card type
	 * @return the number of that card type including new cards
	 */
	public int getNumDevCardType(Cards card) {
		int count = 0;
		for (int i = 0; i < newCards.size(); i++) {
			if (newCards.get(i) == card)
				count += 1;
		}

		return cards[card.ordinal()] + count;
	}

	/**
	 * Get the number of victory point cards
	 * 
	 * @return the number of victory point cards the player has
	 */
	public int getVictoryCards() {
		return victory;
	}

	/**
	 * Determine if the player has a card to use
	 * 
	 * @return true if the player is allowed to use a card
	 */
	public boolean canUseCard() {
		if (usedCard)
			return false;

		for (int i = 0; i < cards.length; i++) {
			if (cards[i] > 0)
				return true;
		}

		return false;
	}

	/**
	 * Add a development card of the given type
	 * 
	 * @param card
	 *            the card type
	 */
	public void addCard(Cards card, boolean canUse) {
		if (canUse) {
			cards[card.ordinal()] += 1;
			usedCard = false;
		} else {
			newCards.add(card);
		}
	}

	/**
	 * Determine if the player has a particular card
	 * 
	 * @param card
	 *            the card type to check for
	 * @return true if the player has this card type
	 */
	public boolean hasCard(Cards card) {
		return (cards[card.ordinal()] > 0);
	}

	/**
	 * Use a card
	 * 
	 * @param card
	 *            the card type to use
	 * @return true if the card was used successfully
	 */
	public boolean useCard(Cards card) {
		if (!hasCard(card) || usedCard)
			return false;

		switch (card) {
		case SOLDIER:
			boolean hadLargest = (board.getLargestArmyOwner() == this);
			soldiers += 1;
			board.checkLargestArmy(this, soldiers);
			if (!hadLargest && board.getLargestArmyOwner() == this)
				appendAction(R.string.player_largest_army);
			board.robberPhase();
			break;
		case PROGRESS:
			board.progressPhase();
			break;
		case VICTORY:
			return false;
		default:
			break;
		}

		cards[card.ordinal()] -= 1;
		usedCard = true;

		appendAction(R.string.player_used_card, Board
				.getCardStringResource(card));

		return true;
	}

	/**
	 * Steal all resources of a given type from the other players
	 * 
	 * @param type
	 */
	public int monopoly(Type type) {
		appendAction(R.string.player_monopoly, Hexagon
				.getTypeStringResource(type));

		int total = 0;

		for (int i = 0; i < 4; i++) {
			Player player = board.getPlayer(i);
			int count = player.getResources(type);

			if (player == this || count <= 0)
				continue;

			player.useResources(type, count);
			addResources(type, count);
			total += count;

			appendAction(R.string.player_stole_from, player.getName());
		}

		return total;
	}

	/**
	 * Get 2 free resources
	 * 
	 * @param type1
	 *            first resoruce type
	 * @param type2
	 *            second resource type
	 */
	public void harvest(Type type1, Type type2) {
		addResources(type1, 1);
		addResources(type2, 1);

		appendAction(R.string.player_got_resource, Hexagon
				.getTypeStringResource(type1));
		appendAction(R.string.player_got_resource, Hexagon
				.getTypeStringResource(type2));
	}

	/**
	 * Get the number of cards that are required to trade for 1 resource
	 * 
	 * @return the number of resources cards needed
	 */
	public int getTradeValue() {
		return tradeValue;
	}

	/**
	 * Determine if the player has a particular trader type
	 * 
	 * @param type
	 *            the resource type, or null for 3:1 trader
	 * @return
	 */
	public boolean hasTrader(Type type) {
		if (type == null)
			return (tradeValue == 3);

		return traders[type.ordinal()];
	}

	/**
	 * Add a trader
	 * 
	 * @param type
	 *            the trader type
	 */
	public void setTradeValue(Type type) {
		// 3:1 trader
		if (type == Type.ANY) {
			tradeValue = 3;
			return;
		}

		// specific trader
		if (type != null)
			traders[type.ordinal()] = true;
	}

	/**
	 * Determine if a trade is valid
	 * 
	 * @param type
	 *            the type to trade for
	 * @param trade
	 *            an array of the number of each card type offered
	 * @return true if the trade is valid
	 */
	public boolean canTrade(Type type, int[] trade) {
		int value = 0;
		for (int i = 0; i < Hexagon.TYPES.length; i++) {
			if (Hexagon.TYPES[i] == Type.DESERT)
				continue;

			// check for specific 2:1 trader
			if (hasTrader(Hexagon.TYPES[i])
					&& getResources(Hexagon.TYPES[i]) >= 2 && trade[i] >= 2)
				return true;

			// deduct from number of resource cards needed
			int number = getResources(Hexagon.TYPES[i]);
			if (number >= trade[i])
				value += trade[i];
		}

		return (value >= tradeValue);
	}

	/**
	 * Trade for a resource
	 * 
	 * @param type
	 *            the type to trade for
	 * @param trade
	 *            an array of the number of each card type offered
	 * @return true if the trade was performed successfully
	 */
	public boolean trade(Type type, int[] trade) {
		// validate trade
		if (!canTrade(type, trade))
			return false;

		// check for 2:1 trader
		for (int i = 0; i < trade.length; i++) {
			if (Hexagon.TYPES[i] == Type.DESERT)
				continue;

			// check for specific 2:1 trader
			if (hasTrader(Hexagon.TYPES[i])
					&& getResources(Hexagon.TYPES[i]) >= 2 && trade[i] >= 2) {
				addResources(type, 1);
				useResources(Hexagon.TYPES[i], 2);
				return true;
			}
		}

		// normal 4:1 or 3:1 trade
		int value = tradeValue;
		for (int i = 0; i < trade.length; i++) {
			if (Hexagon.TYPES[i] == Type.DESERT)
				continue;

			int number = getResources(Hexagon.TYPES[i]);

			// deduct from number of resource cards needed
			if (trade[i] >= value && number >= value) {
				useResources(Hexagon.TYPES[i], value);
				addResources(type, 1);

				appendAction(R.string.player_traded_for, Hexagon
						.getTypeStringResource(type));

				for (int j = 0; j < value; j++) {
					appendAction(R.string.player_traded_away, Hexagon
							.getTypeStringResource(Hexagon.TYPES[i]));
				}

				return true;
			} else if (trade[i] > 0 && number >= trade[i]) {
				useResources(Hexagon.TYPES[i], trade[i]);
				value -= trade[i];
			}
		}

		// this shouldn't happen
		return false;
	}

	/**
	 * Find all possible trade combinations
	 * 
	 * @param want
	 *            the type of resource to trade for
	 * @return a Vector of arrays of the number of each card type to offer
	 */
	public Vector<int[]> findTrades(Type want) {
		Vector<int[]> offers = new Vector<int[]>();

		// generate trades for 2:1 traders
		for (int i = 0; i < Hexagon.TYPES.length; i++) {
			if (Hexagon.TYPES[i] == Type.DESERT || Hexagon.TYPES[i] == want
					|| !hasTrader(Hexagon.TYPES[i]))
				continue;

			int[] trade = new int[Hexagon.TYPES.length];
			trade[i] = 2;

			if (canTrade(want, trade))
				offers.add(trade);
		}

		// generate 3:1 or 4:1 trades
		if (!mixedTrade) {
			for (int i = 0; i < Hexagon.TYPES.length; i++) {
				if (Hexagon.TYPES[i] == Type.DESERT || Hexagon.TYPES[i] == want
						|| hasTrader(Hexagon.TYPES[i]))
					continue;

				int[] trade = new int[Hexagon.TYPES.length];
				trade[i] = tradeValue;

				if (canTrade(want, trade))
					offers.add(trade);
			}

			return offers;
		}

		// generate all combinations of valid mixed-type trades
		int max = getTradeValue();
		for (int i = 0; i <= max; i++) {
			for (int j = 0; j <= max - i; j++) {
				for (int k = 0; k <= max - i - j; k++) {
					for (int l = 0; l <= max - i - j - k; l++) {
						int[] trade = new int[Hexagon.TYPES.length];
						trade[4] = i;
						trade[3] = j;
						trade[2] = k;
						trade[1] = l;
						trade[0] = max - i - j - k - l;

						// ignore trades involving the desired resource
						if (trade[want.ordinal()] != 0)
							continue;

						boolean good = true;
						for (int m = 0; m < Hexagon.TYPES.length; m++) {
							if (hasTrader(Hexagon.TYPES[m]) && trade[m] >= 2)
								good = false;
						}

						if (good && canTrade(want, trade))
							offers.add(trade);
					}
				}
			}
		}

		return offers;
	}

	/**
	 * Determine if the player is a human player on this device
	 * 
	 * @return true if the player is human controlled on this device
	 */
	public boolean isHuman() {
		return (type == PLAYER_HUMAN);
	}

	/**
	 * Determine if the player is a bot
	 * 
	 * @return true if the player is a bot
	 */
	public boolean isBot() {
		return (type == PLAYER_BOT);
	}

	/**
	 * Determine if the player is an online player
	 * 
	 * @return
	 */
	public boolean isOnline() {
		return (type == PLAYER_ONLINE);
	}

	/**
	 * Set the player's name
	 * 
	 * @param name
	 *            the player's new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the player's name
	 * 
	 * @return the player's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Notify the player the the road length is being recalculated
	 */
	public void cancelRoadLength() {
		roadLength = 0;
	}

	/**
	 * Notify the player of a road length and update its longest road length if
	 * greater
	 * 
	 * @param roadLength
	 *            the length of a road
	 */
	public void setRoadLength(int roadLength) {
		if (roadLength > this.roadLength)
			this.roadLength = roadLength;
	}

	/**
	 * Get the length of the player's longest road
	 * 
	 * @return the longest road length
	 */
	public int getRoadLength() {
		return roadLength;
	}

	/**
	 * Get the number of soldiers in the player's army
	 * 
	 * @return the number of soldier cards used
	 */
	public int getArmySize() {
		return soldiers;
	}

	/**
	 * Get the number of towns
	 * 
	 * @return the number of towns the player has
	 */
	public int getNumTowns() {
		return towns;
	}

	/**
	 * Get the number of development cards the player has
	 * 
	 * @return the number of development cards the player has
	 */
	public int getNumDevCards() {
		int count = 0;
		for (int i = 0; i < cards.length; i++)
			count += cards[i];

		return count + newCards.size();
	}

	/**
	 * Get the number of resource cards the player has
	 * 
	 * @return the number of resource cards the player has
	 */
	public int getNumResources() {
		int count = 0;
		for (int i = 0; i < resources.length; i++)
			count += resources[i];

		return count;
	}

	/**
	 * Get the number of cities
	 * 
	 * @return the number of cities the player has
	 */
	public int getNumCities() {
		return cities;
	}

	/**
	 * Get the number of roads built
	 * 
	 * @return the number of roads the player built
	 */
	public int getNumRoads() {
		return roads.size();
	}

	/**
	 * Add an action to the turn log
	 * 
	 * @param action
	 *            a string of the action
	 */
	private void appendAction(String action) {
		if (board.isSetupPhase())
			return;

		if (actionLog == "")
			actionLog += "→ " + action;
		else
			actionLog += "\n" + "→ " + action;

	}

	/**
	 * Add an action to the turn log using a resource string
	 * 
	 * @param action
	 *            string resource id for action
	 */
	private void appendAction(int action) {
		Context context = Settlers.getInstance().getContext();
		appendAction(context.getString(action));
	}

	/**
	 * Add an action to the turn log using a resource string and supplementary
	 * string
	 * 
	 * @param action
	 *            string resource id for action
	 * @param additional
	 *            string to substitute into %s in action
	 */
	private void appendAction(int action, String additional) {
		Context context = Settlers.getInstance().getContext();
		appendAction(String.format(context.getString(action), additional));
	}

	/**
	 * Add an action to the turn log using a resource string and supplementary
	 * string
	 * 
	 * @param action
	 *            string resource id for action
	 * @param additional
	 *            string resource to substitute into %s in action
	 */
	private void appendAction(int action, int additional) {
		Context context = Settlers.getInstance().getContext();
		appendAction(String.format(context.getString(action), context
				.getString(additional)));
	}

	/**
	 * Get the action log
	 * 
	 * @return a String containing the log
	 */
	public String getActionLog() {
		return actionLog;
	}

	/**
	 * Get the string resource for a color
	 * 
	 * @param color
	 *            the color
	 * @return the string resource
	 */
	public static int getColorStringResource(Color color) {
		switch (color) {
		case RED:
			return R.string.red;
		case BLUE:
			return R.string.blue;
		case GREEN:
			return R.string.green;
		case ORANGE:
			return R.string.orange;
		default:
			return R.string.nostring;
		}
	}

	/**
	 * Enabled mixed trades
	 * 
	 * @param mixed
	 *            whether mixed trades should be allowed
	 */
	public static void enableMixedTrades(boolean mixed) {
		mixedTrade = mixed;
	}

	/**
	 * Determine if mixed trades are allowed
	 * 
	 * @return true if mixed trades are allowed
	 */
	public static boolean canTradeMixed() {
		return mixedTrade;
	}
}
