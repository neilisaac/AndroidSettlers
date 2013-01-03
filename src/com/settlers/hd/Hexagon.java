package com.settlers.hd;

import java.util.Vector;

public class Hexagon {

	public static final int NUM_HEXAGONS = 19;

	public enum Type {
		LUMBER, WOOL, GRAIN, BRICK, ORE, DESERT, ANY, LIGHT, DIM, SHORE
	}

	public static final Type[] TYPES = { Type.LUMBER, Type.WOOL, Type.GRAIN,
			Type.BRICK, Type.ORE };

	public final static int[] RESOURCE_COUNT = { 4, 4, 4, 3, 3, 1 };

	private final static int[] PROBABILITY = { 0, 0, 1, 2, 3, 4, 5, 6, 5, 4, 3,
			2, 1 };

	private final static int[] SUM_COUNT = { 0, 0, 1, 2, 2, 2, 2, 0, 2, 2, 2,
			2, 1 };

	private Board board;
	private int roll;
	private Type type;
	private Vertex[] vertex;
	private int id;

	/**
	 * Initialize the hexagon with a resource type and roll number
	 * 
	 * @param type
	 *            resource type
	 * @param index
	 *            id number for the hexagon
	 */
	public Hexagon(Board board, Type type, int index) {
		this.board = board;
		this.type = type;
		this.roll = 0;
		vertex = new Vertex[6];
		id = index;
	}

	/**
	 * Set the connected vertices for hexagon
	 * 
	 * @param v1
	 *            first vertex
	 * @param v2
	 *            second vertex
	 * @param v3
	 *            third vertex
	 * @param v4
	 *            fourth vertex
	 * @param v5
	 *            fifth vertex
	 * @param v6
	 *            sixth vertex
	 */
	public void setVertices(Vertex v1, Vertex v2, Vertex v3, Vertex v4,
			Vertex v5, Vertex v6) {
		vertex[0] = v1;
		vertex[1] = v2;
		vertex[2] = v3;
		vertex[3] = v4;
		vertex[4] = v5;
		vertex[5] = v6;

		for (int i = 0; i < 6; i++)
			vertex[i].addHexagon(this);
	}

	/**
	 * Get the resource type
	 * 
	 * @return the resource type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Set the hexagon type retrospectively
	 * 
	 * @param type
	 *            the hexagon type
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Get a vertex of the hexagon
	 * 
	 * @param index
	 *            the index of the vertex
	 * @return the vertex
	 */
	public Vertex getVertex(int index) {
		return vertex[index];
	}

	/**
	 * Get the roll number for this resource
	 * 
	 * @return the roll sum corresponding to this resource
	 */
	public int getRoll() {
		return roll;
	}

	/**
	 * Set the roll number for this resource
	 * 
	 * @param roll
	 *            the dice sum for this resource
	 */
	public void setRoll(int roll) {
		this.roll = roll;
	}

	/**
	 * Get the probability of rolling this number
	 * 
	 * @return the number of possible rolls which give this number
	 */
	public int getProbability() {
		return PROBABILITY[roll];
	}

	/**
	 * Distribute resources from this hexagon
	 * 
	 * @param roll
	 *            the dice sum
	 */
	public void distributeResources(int roll) {
		if (roll != this.roll || hasRobber())
			return;

		for (int i = 0; i < 6; i++)
			vertex[i].distributeResources(type);
	}

	/**
	 * Check if a player has a town or city adjacent to the hexagon
	 * 
	 * @param player
	 *            the player to check for
	 * @return true if player has a town or city adjacent to the hexagon
	 */
	public boolean hasPlayer(Player player) {
		for (int i = 0; i < 6; i++) {
			if (vertex[i].getOwner() == player)
				return true;
		}

		return false;
	}

	/**
	 * Get the players the own a settlement adjacent to the hexagon
	 * 
	 * @return a Vector of players
	 */
	public Vector<Player> getPlayers() {
		Vector<Player> players = new Vector<Player>();
		for (int i = 0; i < 6; i++) {
			Player owner = vertex[i].getOwner();
			if (owner != null && !players.contains(owner))
				players.add(owner);
		}

		return players;
	}

	/**
	 * Check if this hexagon is adjacent to a given hexagon
	 * 
	 * @param hexagon
	 *            the hexagon to check for
	 * @return true if hexagon is adjacent to this hexagon
	 */
	public boolean isAdjacent(Hexagon hexagon) {
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 3; j++) {
				Hexagon adjacent = vertex[i].getHexagon(j);
				if (adjacent == null || adjacent == this)
					continue;

				if (hexagon == adjacent)
					return true;
			}
		}

		return false;
	}

	/**
	 * Check if the hexagon has the robber
	 * 
	 * @return true if the hexagon has the robber
	 */
	public boolean hasRobber() {
		return (board.getRobber() == this);
	}

	/**
	 * Get the hexagon id
	 * 
	 * @return the hexagon id number
	 */
	public int getId() {
		return id;
	}

	/**
	 * Initialize the hexagons randomly
	 * 
	 * @param board
	 *            the board
	 * @return a hexagon array
	 */
	public static Hexagon[] initialize(Board board) {
		Hexagon[] hexagon = new Hexagon[NUM_HEXAGONS];

		// generate random board layout
		for (int type = 0; type < RESOURCE_COUNT.length; type++) {
			for (int count = 0; count < RESOURCE_COUNT[type]; count++) {

				// pick hexagon index (location)
				while (true) {
					int index = (int) (NUM_HEXAGONS * Math.random());
					if (hexagon[index] == null) {
						Type hexType = Type.values()[type];
						hexagon[index] = new Hexagon(board, hexType, index);

						if (hexType == Type.DESERT) {
							hexagon[index].setRoll(7);
							board.setRobber(index);
						}

						break;
					}
				}
			}
		}

		return hexagon;
	}

	/**
	 * Initialize the hexagons based on a predefined board layout
	 * 
	 * @param board
	 *            the board
	 * @param types
	 *            an array of NUM_HEXAGONS hexagon types
	 * @return a hexagon array
	 */
	public static Hexagon[] initialize(Board board, Hexagon.Type[] types) {
		Hexagon[] hexagon = new Hexagon[NUM_HEXAGONS];
		for (int i = 0; i < hexagon.length; i++)
			hexagon[i] = new Hexagon(board, types[i], i);

		return hexagon;
	}

	/**
	 * Assign roll numbers to the hexagons randomly
	 * 
	 * @param hexagon
	 *            the hexagon array
	 */
	public static void assignRoles(Hexagon[] hexagon) {

		// initialize count of dice sums used to allocate roll numbers
		int[] rollCount = new int[SUM_COUNT.length];
		for (int i = 0; i < rollCount.length; i++)
			rollCount[i] = 0;

		// place 6s and 8s (high probability rolls)
		Hexagon[] highRollers = new Hexagon[4];
		for (int i = 0; i < 4; i++) {
			// pick a random hexagon
			int pick = -1;
			while (pick < 0) {
				pick = (int) (Hexagon.NUM_HEXAGONS * Math.random());

				// make sure it isn't adjacent to another high roller
				for (int j = 0; j < i; j++) {
					if (hexagon[pick].isAdjacent(highRollers[j])) {
						pick = -1;
						break;
					}
				}

				// make sure it wasn't already picked
				if (pick >= 0 && hexagon[pick].getRoll() > 0 || pick >= 0
						&& hexagon[pick].getType() == Type.DESERT)
					pick = -1;
			}

			// assign the roll value
			int roll = (i < 2 ? 6 : 8);
			highRollers[i] = hexagon[pick];
			highRollers[i].setRoll(roll);
			rollCount[roll] += 1;
		}

		// generate random placement of roll numbers
		for (int i = 0; i < Hexagon.NUM_HEXAGONS; i++) {
			// skip hexagons that already have a roll number
			if (hexagon[i].getRoll() > 0 || hexagon[i].getType() == Type.DESERT)
				continue;

			// pick roll
			int roll = 0;
			while (true) {
				roll = (int) (SUM_COUNT.length * Math.random());
				if (rollCount[roll] < SUM_COUNT[roll])
					break;
			}

			hexagon[i].setRoll(roll);
			rollCount[roll] += 1;
		}
	}

	public static Type getType(String string) {
		for (int i = 0; i < Hexagon.TYPES.length; i++) {
			if (string == Hexagon.TYPES[i].toString().toLowerCase())
				return Hexagon.TYPES[i];
		}

		return null;
	}

	public static int getTypeStringResource(Type type) {
		switch (type) {
		case LUMBER:
			return R.string.lumber;
		case WOOL:
			return R.string.wool;
		case GRAIN:
			return R.string.grain;
		case BRICK:
			return R.string.brick;
		case ORE:
			return R.string.ore;
		default:
			return R.string.nostring;
		}
	}
}
