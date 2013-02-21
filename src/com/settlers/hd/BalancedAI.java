package com.settlers.hd;

import java.util.Vector;

import com.settlers.hd.Board.Cards;
import com.settlers.hd.Hexagon.Type;

public class BalancedAI extends Player implements AutomatedPlayer {

	protected static final int[] preference = { 9, 8, 8, 10, 7 };

	public BalancedAI(Board board, int index, Color color, String name) {
		super(board, index, color, name, Player.PLAYER_BOT);
	}

	@Override
	public void buildPhase() {
		boolean done = false;
		while (!done) {
			done = true;

			boolean hasLongest = board.getLongestRoadOwner() == this;
			boolean settlementPriority = towns + cities < 4;
			boolean roadContender = board.getLongestRoad() - getRoadLength() <= 3;

			boolean canSettle = false;
			boolean canCity = false;

			// check if we have a location to build a town or city
			for (int i = 0; i < roads.size(); i++) {
				Vertex v1 = roads.get(i).getVertex1();
				Vertex v2 = roads.get(i).getVertex2();

				if (v1.canBuild(this, Vertex.TOWN)
						|| v2.canBuild(this, Vertex.TOWN))
					canSettle = true;

				if (v1.canBuild(this, Vertex.CITY)
						|| v2.canBuild(this, Vertex.CITY))
					canCity = true;
			}

			// don't uselessly expand roads until player has 4 towns/cities
			boolean considerRoad = getNumRoads() < MAX_ROADS
					&& (!canSettle || (!hasLongest && roadContender && !settlementPriority));

			// try to build a town
			if (canSettle && affordTown()) {
				Vertex pick = pickTown();
				if (pick != null && build(pick, Vertex.TOWN))
					done = false;
			}

			// try to build a city
			if (canCity && affordCity()) {
				for (int i = 0; i < settlements.size(); i++) {
					Vertex settlement = settlements.get(i);
					if (settlement.getBuilding() == Vertex.TOWN
							&& build(settlement, Vertex.CITY))
						done = false;
				}
			}

			// try to build a road if we can afford it
			if (considerRoad && affordRoad()) {
				boolean builtRoad = false;

				// try to build towards somewhere nearby to settle
				if (settlementPriority) {
					for (int i = 0; i < reaching.size(); i++) {
						Vertex vertex = reaching.get(i);
						for (int j = 0; j < 3; j++) {
							Edge edge = vertex.getEdge(j);
							if (edge == null || edge.hasRoad())
								continue;

							Vertex other = edge.getAdjacent(vertex);
							if (!other.hasBuilding() && other.couldBuild()
									&& build(edge)) {
								builtRoad = true;
								break;
							}
						}

						if (builtRoad)
							break;
					}
				}

				// build off an existing road
				if (!builtRoad && reaching.size() > 0) {
					for (int i = 0; i < 3; i++) {
						Vertex lastRoadEnd = reaching.get(reaching.size() - 1);
						Edge edge = lastRoadEnd.getEdge(i);
						if (edge != null && build(edge))
							builtRoad = true;
					}
				}

				// try and add to another recent road
				if (!builtRoad) {
					for (int i = roads.size() - 1; i >= 0; i--) {
						Edge road = roads.get(i);
						Vertex v1 = road.getVertex1();
						Vertex v2 = road.getVertex2();

						for (int j = 0; j < 3; j++) {
							Edge edge1 = v1.getEdge(j);
							Edge edge2 = v2.getEdge(j);

							if (build(edge1) || build(edge2))
								done = false;
						}
					}
				}
			}

			// buy a card if we can afford it
			if (affordCard()) {
				if (buyCard() != null)
					done = false;
			}

			// trade in order to buy something
			if (done) {
				// harvest card
				if (canUseCard() && useCard(Cards.HARVEST)) {
					Type pick = pickResourceType();
					harvest(pick, pick);
					done = false;
				}

				// monopoly card
				else if (canUseCard() && useCard(Cards.MONOPOLY)) {
					Type pick = pickResourceType();
					monopoly(pick);
					done = false;
				}

				// progress card
				else if (canUseCard() && useCard(Cards.PROGRESS)) {
					done = false;
				}

				// trade road road resources
				else if (considerRoad && !affordRoad() && tradeFor(ROAD_COST)) {
					if (affordRoad())
						done = false;
				}

				// trade for town resources
				else if (canSettle && !affordTown() && tradeFor(TOWN_COST)) {
					if (affordTown())
						done = false;
				}

				// trade for city resources
				else if (canCity && !affordCity() && !settlementPriority
						&& tradeFor(CITY_COST)) {
					if (affordCity())
						done = false;
				}

				// trade for card resources
				else if (!affordCard() && !settlementPriority
						&& tradeFor(CARD_COST)) {
					if (affordCard())
						done = false;
				}
			}
		}
	}

	private Type pickResourceType() {
		Type pick = Type.BRICK;
		int min = 100;

		// pick the resource that the player has the least of
		for (int i = 0; i < Hexagon.TYPES.length; i++) {
			Type type = Hexagon.TYPES[i];
			int number = getResources(type);

			if (number < min) {
				min = number;
				pick = type;
			}
		}

		return pick;
	}

	@Override
	public void productionPhase() {
		// use soldier card before rolling if the robber is on useful resource
		if (board.getRobber().hasPlayer(this) && hasCard(Cards.SOLDIER))
			useCard(Cards.SOLDIER);
	}

	@Override
	public int setupRoad(Edge[] edges) {
		// build from random settlement and build in a random direction
		while (true) {
			Vertex vertex = settlements.get((int) (Math.random() * settlements
					.size()));
			int pick = (int) (Math.random() * 3);
			Edge edge = vertex.getEdge(pick);
			if (edge != null && build(edge))
				return edge.getIndex();
		}
	}

	@Override
	public int progressRoad(Edge[] edges) {
		Edge road = pickRoad();
		if (road == null)
			return -1;

		Vertex from = road.getVertex1();
		if (from.getOwner() == this || from.hasRoad(this) && build(road))
			return road.getIndex();

		from = road.getVertex2();
		if (from.getOwner() == this || from.hasRoad(this) && build(road))
			return road.getIndex();

		return -1;
	}

	@Override
	public int setupTown(Vertex[] vertices) {
		int highest = 0;
		int index = 0;

		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i].getOwner() != null)
				continue;

			int score = vertexValue(vertices[i], preference);
			if (score > highest && canBuild(vertices[i], Vertex.TOWN)) {
				highest = score;
				index = i;
			}
		}

		build(vertices[index], Vertex.TOWN);
		reaching.add(vertices[index]);
		return index;
	}

	@Override
	public int placeRobber(Hexagon[] hexagons, Hexagon exception) {
		int best = 0, highest = 0;
		for (int i = 0; i < hexagons.length; i++) {
			int value = hexagonValue(hexagons[i], preference);
			if (value <= highest || hexagons[i] == exception)
				continue;

			Vector<Player> players = hexagons[i].getPlayers();
			boolean canSteal = false;
			for (int j = 0; j < players.size(); j++) {
				if (players.get(j) == this)
					value = 1;
				else if (players.get(j).getResourceCount() > 0)
					canSteal = true;
			}

			if (canSteal && value > highest) {
				best = i;
				highest = value;
			}
		}

		return best;
	}

	@Override
	public int steal(Player[] players) {
		// steal from the first player that has resources
		for (int i = 0; i < players.length; i++) {
			if (players[i].getResourceCount() > 0)
				return i;
		}

		return 0;
	}

	protected Vertex pickTown() {
		if (!affordTown())
			return null;

		Vertex best = null;
		int highest = 0;

		for (int i = 0; i < roads.size(); i++) {
			Vertex v1 = roads.get(i).getVertex1();
			Vertex v2 = roads.get(i).getVertex2();

			int value1 = v1.canBuild(this, Vertex.TOWN) ? vertexValue(v1,
					preference) : 0;
			int value2 = v2.canBuild(this, Vertex.TOWN) ? vertexValue(v2,
					preference) : 0;

			if (value1 > value2 && value1 > highest) {
				highest = value1;
				best = v1;
			} else if (value2 > highest) {
				highest = value2;
				best = v2;
			}
		}

		return best;
	}

	protected Edge pickRoad() {
		// build off an existing road
		if (reaching.size() > 0) {
			Vertex lastRoadEnd = reaching.get(reaching.size() - 1);
			for (int i = 0; i < 3; i++) {
				Edge edge = lastRoadEnd.getEdge(i);
				if (edge != null && canBuild(edge))
					return edge;
			}
		}

		// try and add to another recent road
		for (int i = roads.size() - 1; i >= 0; i--) {
			Edge road = roads.get(i);
			Vertex v1 = road.getVertex1();
			Vertex v2 = road.getVertex2();

			for (int j = 0; j < 3; j++) {
				// null checking is in canBuild

				Edge edge1 = v1.getEdge(j);
				if (canBuild(edge1))
					return edge1;

				Edge edge2 = v2.getEdge(j);
				if (canBuild(edge2))
					return edge2;
			}
		}

		return null;
	}

	protected int hexagonValue(Hexagon hexagon, int[] factors) {
		Type type = hexagon.getType();
		if (factors != null && type != Type.DESERT)
			return factors[type.ordinal()] * hexagon.getProbability();
		else
			return hexagon.getProbability();
	}

	protected int vertexValue(Vertex vertex, int[] factors) {
		int value = 1;
		for (int i = 0; i < 3; i++) {
			Hexagon hexagon = vertex.getHexagon(i);
			if (hexagon != null)
				value += hexagonValue(hexagon, factors);
		}

		return value;
	}

	private boolean tradeFor(int[] want) {
		// copy list of resource we have
		int[] have = new int[Hexagon.TYPES.length];
		for (int i = 0; i < have.length; i++)
			have[i] = getResources(Hexagon.TYPES[i]);

		// create list of resources we need
		int[] need = new int[Hexagon.TYPES.length];
		for (int i = 0; i < need.length; i++)
			need[i] = want[i] - have[i];

		Vector<Type> types = new Vector<Type>();
		Vector<int[]> trades = new Vector<int[]>();

		// for each resource types we need
		for (int i = 0; i < need.length; i++) {
			// for the number of that resource we need
			for (int j = 0; j < need[i]; j++) {
				Vector<int[]> offers = findTrades(Hexagon.TYPES[i]);
				for (int k = 0; k < offers.size(); k++) {
					int[] offer = offers.get(k);

					// check if it uses any resources that are wanted
					// and that we still would have enough resources
					boolean accept = true;
					for (int l = 0; l < offer.length; l++) {
						if (offer[l] > 0 && want[l] > 0 || have[l] < offer[l])
							accept = false;
					}

					// accept the first good offer
					if (accept) {
						// adjust balance
						need[i] -= 1;
						for (int l = 0; l < have.length; l++)
							have[l] -= offer[l];

						// add to list of trades
						types.add(Hexagon.TYPES[i]);
						trades.add(offer);
						break;
					}
				}
			}
		}

		// check if the trades cover everything needed
		for (int i = 0; i < need.length; i++)
			if (need[i] > 0)
				return false;

		// run the trades
		for (int i = 0; i < trades.size(); i++) {
			// abort on first failing trade
			if (!trade(types.get(i), trades.get(i)))
				return false;
		}

		return true;
	}

	private void addList(int[] a, int[] b) {
		for (int i = 0; i < a.length; i++)
			a[i] += b[i];
	}

	private void subtractList(int[] a, int[] b) {
		for (int i = 0; i < a.length; i++)
			a[i] += b[i];
	}

	private int compareList(int[] a, int[] b) {
		boolean greater = false;

		for (int i = 0; i < a.length; i++) {
			if (a[i] < b[i])
				return -1;
			else if (a[i] > b[i])
				greater = true;
		}

		if (greater)
			return 1;

		return 0;
	}

	@Override
	public int[] offerTrade(Player player, Type type, int[] offer) {
		// don't try to trade for a resource we don't have
		if (getResources(type) <= 0)
			return null;

		// don't trade with players who may be about to win
		int points = player.getPublicVictoryPoints();
		int max = board.getMaxPoints();
		if (max - points <= 1)
			return null;

		// get a resource list
		int[] extra = getResources();

		// deduct anything that we can already build
		do {
			if (affordCity()) {
				subtractList(extra, CITY_COST);
				continue;
			} else if (affordTown()) {
				subtractList(extra, TOWN_COST);
				continue;
			} else if (affordCard()) {
				subtractList(extra, CARD_COST);
				continue;
			} else if (affordRoad()) {
				subtractList(extra, ROAD_COST);
				continue;
			}
		} while (false);

		// don't trade if that resource is needed for something we can build
		if (extra[type.ordinal()] <= 0)
			return null;

		// add in the offer
		addList(extra, offer);

		// see if we can build anything new
		if ((compareList(extra, ROAD_COST) >= 0)
				|| (compareList(extra, TOWN_COST) >= 0)
				|| (compareList(extra, CITY_COST) >= 0)
				|| (compareList(extra, CARD_COST) >= 0))
			return offer;

		return null;
	}

	@Override
	public void discard(int quantity) {
		// get a resource list
		int[] extra = getResources();
		int count = getResourceCount() - quantity;

		// deduct anything that we can already build
		do {
			if (affordCity() && count >= 5) {
				subtractList(extra, CITY_COST);
				count -= 5;
				continue;
			} else if (affordTown() && count >= 4) {
				subtractList(extra, TOWN_COST);
				count -= 4;
				continue;
			} else if (affordCard() && count >= 3) {
				subtractList(extra, CARD_COST);
				count -= 3;
				continue;
			} else if (affordRoad() && count >= 2) {
				subtractList(extra, ROAD_COST);
				count -= 2;
				continue;
			}
		} while (false);

		// discard 'quantity' resources
		for (int q = 0; q < quantity; q++) {

			// try to pick the most common resource
			int max = 0;
			Type mostCommon = null;
			for (int i = 0; i < extra.length; i++) {
				if (extra[i] > max) {
					max = extra[i];
					mostCommon = Hexagon.TYPES[i];
				}
			}

			if (mostCommon != null)
				extra[mostCommon.ordinal()] -= 1;

			// discard the most common resource, or a random resource
			super.discard(mostCommon);
		}
	}
}
