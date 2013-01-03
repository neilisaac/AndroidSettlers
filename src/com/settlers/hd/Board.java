package com.settlers.hd;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.Vector;

public class Board {

	public enum Cards {
		SOLDIER, PROGRESS, HARVEST, MONOPOLY, VICTORY
	}

	private static final int NUM_SOLDIER = 14;
	private static final int NUM_PROGRESS = 2;
	private static final int NUM_HARVEST = 2;
	private static final int NUM_MONOPOLY = 2;
	private static final int NUM_VICTORY = 5;

	private enum Phase {
		SETUP1S, SETUP1R, SETUP2S, SETUP2R, PRODUCTION, BUILD, PROGRESS1, PROGRESS2, ROBBER, DONE
	}

	private Phase phase, returnPhase;

	private Hexagon[] hexagon;
	private Vertex[] vertex;
	private Edge[] edge;
	private Player[] player;
	private Trader[] trader;
	private int[] cards;
	private Stack<Player> discardQueue;
	private Vector<String> eventList;

	private int robber, turn, turnNumber, roadCountId, longestRoad,
			largestArmy, maxPoints, humans, robberLast, lastRoll;
	private Player longestRoadOwner, largestArmyOwner, winner;

	private boolean autoDiscard;

	private OnlineGame online;

	/**
	 * Create new board layout
	 * 
	 * @param names
	 *            array of player names
	 * @param human
	 *            whether each player is human
	 */
	public Board(String[] names, boolean[] human, int maxPoints,
			boolean autoDiscard) {
		this.maxPoints = maxPoints;
		commonInit();
		online = null;

		this.autoDiscard = autoDiscard;

		// initialise players
		player = new Player[4];
		for (int i = 0; i < 4; i++)
			player[i] = null;

		humans = 0;
		for (int i = 0; i < 4; i++) {
			while (true) {
				int pick = (int) (Math.random() * 4);
				if (player[pick] != null)
					continue;

				Player.Color color = Player.Color.values()[i];

				if (human[i]) {
					humans += 1;
					player[pick] = new Player(this, pick, color, names[i],
							Player.PLAYER_HUMAN);
				} else {
					player[pick] = new BalancedAI(this, pick, color, names[i]);
				}

				break;
			}
		}
	}

	private void commonInit() {
		turn = 0;
		turnNumber = 1;
		phase = Phase.SETUP1S;
		roadCountId = 0;
		longestRoad = 4;
		largestArmy = 2;
		longestRoadOwner = null;
		largestArmyOwner = null;
		hexagon = null;
		winner = null;

		discardQueue = new Stack<Player>();
		eventList = new Vector<String>();

		// initialize development cards
		cards = new int[Cards.values().length];
		cards[Cards.SOLDIER.ordinal()] = NUM_SOLDIER;
		cards[Cards.PROGRESS.ordinal()] = NUM_PROGRESS;
		cards[Cards.VICTORY.ordinal()] = NUM_VICTORY;
		cards[Cards.HARVEST.ordinal()] = NUM_HARVEST;
		cards[Cards.MONOPOLY.ordinal()] = NUM_MONOPOLY;

		// randomly initialize hexagons
		hexagon = Hexagon.initialize(this);
		trader = Trader.initialize();
		vertex = Vertex.initialize();
		edge = Edge.initialize();

		// associate hexagons, vertices, edges, and traders
		Geometry.setAssociations(hexagon, vertex, edge, trader);

		// assign roll numbers randomly
		Hexagon.assignRoles(hexagon);
	}

	/**
	 * Get a reference to the current player
	 * 
	 * @return the current player
	 */
	public Player getCurrentPlayer() {
		if (player == null)
			return null;

		return player[turn];
	}

	/**
	 * Get a player by index
	 * 
	 * @param index
	 *            player index [0, 3]
	 * @return the player
	 */
	public Player getPlayer(int index) {
		return player[index];
	}

	/**
	 * Distribute resources for a given roll number
	 * 
	 * @param roll
	 *            the roll number
	 */
	public void roll(int roll) {
		if (roll == 7) {
			// reduce each player to 7 cards
			for (int i = 0; i < 4; i++) {
				int cards = player[i].getResourceCount();
				int extra = cards > 7 ? cards / 2 : 0;

				if (extra == 0)
					continue;

				if (autoDiscard) {
					// discard randomly
					for (int j = 0; j < extra; j++)
						player[i].discard(null);
				}
				if (player[i].isBot()) {
					// instruct the ai to discard
					AutomatedPlayer bot = (AutomatedPlayer) player[i];
					bot.discard(extra);
				} else if (player[i].isHuman()) {
					// queue human player to discard
					discardQueue.add(player[i]);
				}
			}

			// enter robber phase
			robberPhase();
		} else {
			// distribute resources
			for (int i = 0; i < hexagon.length; i++)
				hexagon[i].distributeResources(roll);
		}

		lastRoll = roll;
		addRollEvent(getCurrentPlayer(), roll);
	}

	/**
	 * Get the last roll
	 * 
	 * @return the last roll, or 0
	 */
	public int getRoll() {
		if (isSetupPhase() || isProgressPhase())
			return 0;

		return lastRoll;
	}

	/**
	 * Run the AI's robber methods
	 * 
	 * @param current
	 *            current ai player
	 */
	private void aiRobberPhase(AutomatedPlayer current) {
		int hex = current.placeRobber(hexagon, hexagon[robberLast]);
		setRobber(hex);

		int count = 0;
		for (int i = 0; i < 4; i++)
			if (player[i] != player[turn] && hexagon[hex].hasPlayer(player[i]))
				count++;

		if (count > 0) {
			Player[] stealList = new Player[count];
			for (int i = 0; i < 4; i++)
				if (player[i] != player[turn]
						&& hexagon[hex].hasPlayer(player[i]))
					stealList[--count] = player[i];

			int who = current.steal(stealList);
			player[turn].steal(stealList[who]);
		}

		phase = returnPhase;
	}

	/**
	 * Start a player's turn
	 */
	public void runTurn() {
		// process ai turn
		if (player[turn].isBot()) {
			AutomatedPlayer current = (AutomatedPlayer) player[turn];
			switch (phase) {

			case SETUP1S:
			case SETUP2S:
				current.setupTown(vertex);
				break;

			case SETUP1R:
			case SETUP2R:
				current.setupRoad(edge);
				break;

			case PRODUCTION:
				current.productionPhase();
				player[turn].roll();
				break;

			case BUILD:
				current.buildPhase();
				break;

			case PROGRESS1:
				current.progressRoad(edge);
			case PROGRESS2:
				current.progressRoad(edge);
				phase = returnPhase;
				return;

			case ROBBER:
				aiRobberPhase(current);
				return;

			case DONE:
				return;

			}

			nextPhase();
		}

		// process online turn
		else if (online != null && player[turn].isOnline()) {

		}
	}

	/**
	 * Proceed to the next phase or next turn
	 * 
	 * My initial reaction was to treat it as a state machine
	 */
	public boolean nextPhase() {
		boolean turnChanged = false;
		
		addNextPhaseEvent(getCurrentPlayer());

		switch (phase) {
		case SETUP1S:
			phase = Phase.SETUP1R;
			break;
		case SETUP1R:
			if (turn < 3) {
				turn++;
				turnChanged = true;
				phase = Phase.SETUP1S;
			} else {
				phase = Phase.SETUP2S;
			}
			break;
		case SETUP2S:
			phase = Phase.SETUP2R;
			break;
		case SETUP2R:
			if (turn > 0) {
				turn--;
				turnChanged = true;
				phase = Phase.SETUP2S;
			} else {
				phase = Phase.PRODUCTION;
			}
			break;
		case PRODUCTION:
			phase = Phase.BUILD;
			break;
		case BUILD:
			if (turn == 3)
				turnNumber += 1;
			player[turn].endTurn();
			phase = Phase.PRODUCTION;
			turn++;
			turn %= 4;
			turnChanged = true;
			player[turn].beginTurn();
			lastRoll = 0;
			break;
		case PROGRESS1:
			phase = Phase.PROGRESS2;
			break;
		case PROGRESS2:
			phase = returnPhase;
			break;
		case ROBBER:
			phase = returnPhase;
			break;
		case DONE:
			return false;
		}

		return turnChanged;
	}

	/**
	 * Enter progress phase 1 (road building)
	 */
	public void progressPhase() {
		returnPhase = phase;
		phase = Phase.PROGRESS1;
		runTurn();
	}

	/**
	 * Enter the robber placement phase
	 */
	public void robberPhase() {
		robberLast = robber;
		robber = -1;
		returnPhase = phase;
		phase = Phase.ROBBER;
		runTurn();
	}

	/**
	 * Determine if we're in a setup phase
	 * 
	 * @return true if the game is in setup phase
	 */
	public boolean isSetupPhase() {
		return (phase == Phase.SETUP1S || phase == Phase.SETUP1R
				|| phase == Phase.SETUP2S || phase == Phase.SETUP2R);
	}

	public boolean isSetupTown() {
		return (phase == Phase.SETUP1S || phase == Phase.SETUP2S);
	}

	public boolean isSetupRoad() {
		return (phase == Phase.SETUP1R || phase == Phase.SETUP2R);
	}

	public boolean isSetupPhase2() {
		return (phase == Phase.SETUP2S || phase == Phase.SETUP2R);
	}

	public boolean isRobberPhase() {
		return (phase == Phase.ROBBER);
	}

	public boolean isProduction() {
		return (phase == Phase.PRODUCTION);
	}

	public boolean isBuild() {
		return (phase == Phase.BUILD);
	}

	public boolean isProgressPhase() {
		return (phase == Phase.PROGRESS1 || phase == Phase.PROGRESS2);
	}

	public boolean isProgressPhase1() {
		return (phase == Phase.PROGRESS1);
	}

	public boolean isProgressPhase2() {
		return (phase == Phase.PROGRESS2);
	}

	/**
	 * Get the dice roll value for a hexagon
	 * 
	 * @param index
	 *            the index of the hexagon
	 * @return the roll value
	 */
	public int getRoll(int index) {
		return hexagon[index].getRoll();
	}

	/**
	 * Get the resource type for one hexagon
	 * 
	 * @param index
	 *            the index of the hexagon
	 * @return the resource type
	 */
	public Hexagon.Type getResource(int index) {
		return hexagon[index].getType();
	}

	/**
	 * Get indexed hexagon type mapping
	 * 
	 * @return array of resource types
	 * @note this is intended only to be used to stream out the board layout
	 */
	public int[] getMapping() {
		int hexMapping[] = new int[Hexagon.NUM_HEXAGONS];
		for (int i = 0; i < Hexagon.NUM_HEXAGONS; i++)
			hexMapping[i] = hexagon[i].getType().ordinal();

		return hexMapping;
	}

	/**
	 * Get a given hexagon
	 * 
	 * @param index
	 *            the index of the hexagon
	 * @return the hexagon
	 */
	public Hexagon getHexagon(int index) {
		if (index < 0 || index >= Hexagon.NUM_HEXAGONS)
			return null;

		return hexagon[index];
	}

	/**
	 * Get a given trader
	 * 
	 * @param index
	 *            the index of the trader
	 * @return the trader
	 */
	public Trader getTrader(int index) {
		if (index < 0 || index >= Trader.NUM_TRADER)
			return null;

		return trader[index];
	}

	/**
	 * Get the given edge
	 * 
	 * @param index
	 *            the index of the edge
	 * @return the edge
	 */
	public Edge getEdge(int index) {
		if (index < 0 || index >= Edge.NUM_EDGES)
			return null;

		return edge[index];
	}

	/**
	 * Get the given vertex
	 * 
	 * @param index
	 *            the index of the vertex
	 * @return the vertex
	 */
	public Vertex getVertex(int index) {
		if (index < 0 || index >= Vertex.NUM_VERTEX)
			return null;

		return vertex[index];
	}

	/**
	 * Get a development card
	 * 
	 * @return the type of development card or null if that stack is empty
	 */
	public Cards getDevelopmentCard() {
		int soldiers = cards[Cards.SOLDIER.ordinal()];
		int progress = cards[Cards.PROGRESS.ordinal()];
		int victory = cards[Cards.VICTORY.ordinal()];
		int harvest = cards[Cards.HARVEST.ordinal()];
		int monopoly = cards[Cards.MONOPOLY.ordinal()];
		int number = soldiers + progress + victory + harvest + monopoly;

		if (number == 0)
			return null;

		int pick = (int) (Math.random() * number);

		Cards card;
		if (pick < soldiers)
			card = Cards.SOLDIER;
		else if (pick < soldiers + progress)
			card = Cards.PROGRESS;
		else if (pick < soldiers + progress + victory)
			card = Cards.VICTORY;
		else if (pick < soldiers + progress + victory + harvest)
			card = Cards.HARVEST;
		else
			card = Cards.MONOPOLY;

		cards[card.ordinal()] -= 1;
		return card;
	}

	/**
	 * Get the number of points required to win
	 * 
	 * @return the number of points required to win
	 */
	public int getMaxPoints() {
		return maxPoints;
	}

	/**
	 * Update the longest road owner and length
	 */
	public void checkLongestRoad() {
		Player previousOwner = longestRoadOwner;

		// reset road length in case a road was split
		longestRoad = 4;
		longestRoadOwner = null;

		// reset players' road lengths to 0
		for (int i = 0; i < 4; i++)
			player[i].cancelRoadLength();

		// find longest road
		for (int i = 0; i < edge.length; i++) {
			if (edge[i].hasRoad()) {
				int length = edge[i].getRoadLength(++roadCountId);

				Player owner = edge[i].getOwner();
				owner.setRoadLength(length);
				if (length > longestRoad) {
					longestRoad = length;
					longestRoadOwner = owner;
				}
			}
		}

		// the same player keeps the longest road if length doesn't change
		if (previousOwner != null
				&& previousOwner.getRoadLength() == longestRoad)
			longestRoadOwner = previousOwner;
	}

	/**
	 * Determine if player has the longest road
	 * 
	 * @param player
	 *            the player
	 * @return true if player had the longest road
	 */
	public boolean hasLongestRoad(Player player) {
		return (longestRoadOwner != null && player == longestRoadOwner);
	}

	/**
	 * Get the length of the longest road
	 * 
	 * @return the length of the longest road
	 */
	public int getLongestRoad() {
		return longestRoad;
	}

	/**
	 * Get the owner of the longest road
	 * 
	 * @return the player with the longest road
	 */
	public Player getLongestRoadOwner() {
		return longestRoadOwner;
	}

	/**
	 * Update the largest army if the given size is larger than the current size
	 * 
	 * @param player
	 *            the player owning the army
	 * @param size
	 *            the number of soldiers
	 */
	public void checkLargestArmy(Player player, int size) {
		if (size > largestArmy) {
			largestArmyOwner = player;
			largestArmy = size;
		}
	}

	/**
	 * Determine if player has the largest army
	 * 
	 * @param player
	 *            the player
	 * @return true if player has the largest army
	 */
	public boolean hasLargestArmy(Player player) {
		return (largestArmyOwner != null && player == largestArmyOwner);
	}

	/**
	 * Get the size of the largest army
	 * 
	 * @return the size of the largest army
	 */
	public int getLargestArmy() {
		return largestArmy;
	}

	/**
	 * Get the owner of the largest army
	 * 
	 * @return the player with the largest army
	 */
	public Player getLargestArmyOwner() {
		return largestArmyOwner;
	}

	/**
	 * Check if any players need to discard
	 * 
	 * @return true if one or more players need to discard
	 */
	public boolean checkPlayerToDiscard() {
		return !discardQueue.empty();
	}

	/**
	 * Get the next player queued for discarding
	 * 
	 * @return a player or null
	 */
	public Player getPlayerToDiscard() {
		try {
			return discardQueue.pop();
		} catch (EmptyStackException e) {
			return null;
		}
	}

	/**
	 * Get an instruction string for the current phase
	 * 
	 * @return the instruction string resource or 0
	 */
	public int getPhaseResource() {
		switch (phase) {
		case SETUP1S:
			return R.string.phase_first_town;
		case SETUP1R:
			return R.string.phase_first_road;
		case SETUP2S:
			return R.string.phase_second_town;
		case SETUP2R:
			return R.string.phase_second_road;
		case PRODUCTION:
			return R.string.phase_roll_production;
		case BUILD:
			return R.string.phase_build;
		case PROGRESS1:
			return R.string.phase_progress1;
		case PROGRESS2:
			return R.string.phase_progress2;
		case ROBBER:
			return R.string.phase_move_robber;
		case DONE:
			return R.string.phase_game_over;
		}

		return 0;
	}

	/**
	 * Get the global turn number
	 * 
	 * @return the turn number (starting at 1, after setup)
	 */
	public int getTurnNumber() {
		return turnNumber;
	}

	/**
	 * Get the winner
	 * 
	 * @return the winning player or null
	 */
	public Player getWinner(Settings settings) {
		// winner already found or we just want to check what was already found
		if (winner != null || settings == null)
			return winner;

		// check for winner
		for (int i = 0; i < 4; i++) {
			if (player[i].getVictoryPoints() >= maxPoints) {
				phase = Phase.DONE;
				winner = player[i];
				break;
			}
		}

		// save game stats
		if (winner != null)
			settings.addScore(humans, maxPoints, winner.getName(), turnNumber);

		return winner;
	}

	/**
	 * Get the hexagon with the robber
	 * 
	 * @return the hexagon with the robber
	 */
	public Hexagon getRobber() {
		if (robber < 0)
			return null;

		return hexagon[robber];
	}

	/**
	 * If the robber is being moved, return the last hexagon where it last
	 * resided, or otherwise the current location
	 * 
	 * @return the last location of the robber
	 */
	public Hexagon getRobberLast() {
		if (robber < 0 && robberLast >= 0 && robberLast < Hexagon.NUM_HEXAGONS)
			return hexagon[robberLast];
		else if (robber >= 0 && robber < Hexagon.NUM_HEXAGONS)
			return hexagon[robber];
		else
			return null;
	}

	/**
	 * Set the index for the robber
	 * 
	 * @param robber
	 *            id of the hexagon with the robber
	 * @return true if the robber was placed
	 */
	public boolean setRobber(int robber) {
		this.robber = robber;
		if (hexagon != null)
			addRobberEvent(getCurrentPlayer(), hexagon[robber]);
		else
			addEvent("robber 0 " + robber);
		
		return true;
	}

	/**
	 * Get the string resource for a card type
	 * 
	 * @param card
	 *            the card type
	 * @return the string resource
	 */
	public static int getCardStringResource(Cards card) {
		switch (card) {
		case SOLDIER:
			return R.string.soldier;
		case PROGRESS:
			return R.string.progress;
		case VICTORY:
			return R.string.victory;
		case HARVEST:
			return R.string.harvest;
		case MONOPOLY:
			return R.string.monopoly;
		default:
			return R.string.nostring;
		}
	}

	/**
	 * Add an event to the event list
	 * 
	 * @param event
	 *            event string
	 */
	private void addEvent(String event) {
		eventList.add(event);
	}

	public void addNextPhaseEvent(Player player) {
		addEvent("next " + player.getIndex());
	}

	public void addRollEvent(Player player, int roll) {
		addEvent("roll " + player.getIndex() + " " + roll);
	}

	public void addBuildEvent(Player player, Vertex vertex) {
		String building = (vertex.getBuilding() == Vertex.TOWN ? "TOWN"
				: "CITY");
		addEvent("build " + player.getIndex() + " " + building + " "
				+ vertex.getIndex());
	}

	public void addBuildEvent(Player player, Edge edge) {
		addEvent("build " + player.getIndex() + " ROAD " + edge.getIndex());
	}

	public void addResourceEvent(Player player, Hexagon.Type type, int amount) {
		addEvent("resource " + player.getIndex() + " " + type.ordinal() + " "
				+ amount);
	}

	public void addBuyCardEvent(Player player, Cards card) {
		addEvent("buy " + player.getIndex() + " " + card.ordinal());
	}

	public void addCardEvent(Player player, Cards card) {
		addEvent("card " + player.getIndex() + " " + card.ordinal());
	}

	public void addMonopolyEvent(Player player, Hexagon.Type type) {
		addEvent("monopoly " + player.getIndex() + " " + type.ordinal());
	}

	public void addRobberEvent(Player player, Hexagon robber) {
		addEvent("robber " + (player != null ? player.getIndex() : "0") + " "
				+ robber.getId());
	}

	public void addStealEvent(Player to, Player from, Hexagon.Type type) {
		addEvent("steal " + to.getIndex() + " " + from.getIndex() + " "
				+ type.toString());
	}

	/**
	 * Execute an event
	 * 
	 * @param event
	 *            string representing the event
	 */
	public void replayEvent(String event) {
		String[] parts = event.split(" ");
		if (parts.length < 2)
			return;
		
		String action = parts[0];
		
		if (action == "hexagon") {
			int index = Integer.parseInt(parts[1]);
			int type = Integer.parseInt(parts[2]);
			int roll = Integer.parseInt(parts[3]);
			hexagon[index].setType(Hexagon.TYPES[type]);
			hexagon[index].setRoll(roll);
			addEvent(event);
			return;
		}
		
		else if (action == "trader") {
			int index = Integer.parseInt(parts[1]);
			int type = Integer.parseInt(parts[2]);
			trader[index].setType(Hexagon.TYPES[type]);
			addEvent(event);
			return;
		}
		
		else if (action == "setting") {
			// TODO: load game rule settings
			addEvent(event);
			return;
		}
		
		int playerIndex = Integer.parseInt(parts[1]);
		if (playerIndex < 0 || playerIndex > 3)
			return;
		
		Player player = this.player[playerIndex];
		
		// roll dice
		if (action == "roll") {
			roll(Integer.parseInt(parts[2]));
			nextPhase();
		}
		
		// build
		else if (action == "build") {
			String type = parts[2];
			if (type == "ROAD") {
				Edge edge = this.edge[Integer.parseInt(parts[3])];
				player.build(edge);
			} else if (type == "TOWN") {
				Vertex vertex = this.vertex[Integer.parseInt(parts[3])];
				player.build(vertex, Vertex.TOWN);
			} else if (type == "CITY") {
				Vertex vertex = this.vertex[Integer.parseInt(parts[3])];
				player.build(vertex, Vertex.CITY);
			}
		}
		
		// give or take resource
		else if (action == "resource") {
			Hexagon.Type type = Hexagon.TYPES[Integer.parseInt(parts[2])];
			int amount = Integer.parseInt(parts[3]);
			if (amount > 0)
				player.addResources(type, amount);
			else
				player.useResources(type, -amount);
		}
		
		// buy card
		else if (action == "buy") {
			int card = Integer.parseInt(parts[2]);
			player.buyCard(Cards.values()[card]);
		}
		
		// use card
		else if (action == "card") {
			int card = Integer.parseInt(parts[2]);
			player.useCard(Cards.values()[card]);
		}
		
		// monopoly
		else if (action == "monopoly") {
			int type = Integer.parseInt(parts[2]);
			player.monopoly(Hexagon.TYPES[type]);
		}
		
		// move robber
		else if (action == "robber") {
			int index = Integer.parseInt(parts[2]);
			setRobber(index);
		}
		
		// steal
		else if (action == "steal") {
			Player from = this.player[Integer.parseInt(parts[2])];
			int type = Integer.parseInt(parts[3]);
			player.steal(from, Hexagon.TYPES[type]);
		}
		
		// next phase
		else if (action == "next") {
			nextPhase();
		}
	}

	/**
	 * Get a textual dump of the board setup
	 * 
	 * @return string representing the board setup
	 */
	public String dumpSetup() {
		String dump = "";

		for (Hexagon hex : hexagon) {
			dump += "hexagon " + hex.getId() + " " + hex.getType().ordinal()
					+ " " + hex.getRoll() + "\n";
		}

		for (int i = 0; i < trader.length; i++) {
			dump += "trader " + i + " " + trader[i].getType().ordinal() + "\n";
		}
		
		// TODO: save game rule settings

		return dump;
	}

	/**
	 * Get a textual dump of the game events
	 * 
	 * @return string representing each event
	 */
	public String dumpEvents() {
		String dump = "";

		for (String event : eventList)
			dump += event + "\n";

		return dump;
	}
	
	/**
	 * Save the current state of the game into the application database
	 * 
	 * @param settings
	 */
	public void save(Settings settings) {
		String events = dumpSetup() + dumpEvents();
		settings.set("game_events", events);
	}

	/**
	 * Load a game state from the application database
	 * 
	 * @param settings
	 */
	public boolean load(Settings settings) {
		String events = settings.get("game_events");
		if (events == null || events == "")
			return false;
		
		for (String event : events.split("\n"))
			replayEvent(event);
		
		return true;
	}

	/**
	 * Clear any stored game state
	 * 
	 * @param settings
	 */
	public void clear(Settings settings) {
		settings.set("game_events", "");
	}
}
