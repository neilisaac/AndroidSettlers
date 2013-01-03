package com.settlers.hd;

public class Vertex {

	public static final int NUM_VERTEX = 54;

	public static final int NONE = 0;
	public static final int TOWN = 1;
	public static final int CITY = 2;

	private int index;
	private int building;

	private Player owner;

	private Edge[] edge;
	private Hexagon[] hexagon;
	private Trader trader;

	/**
	 * Initialize a vertex with edges set to null
	 * 
	 * @param index
	 *            the vertex index for drawing
	 */
	public Vertex(int index) {
		this.index = index;
		owner = null;
		building = NONE;

		edge = new Edge[3];
		edge[0] = edge[1] = edge[2] = null;

		hexagon = new Hexagon[3];
		hexagon[0] = hexagon[1] = hexagon[2] = null;
		setTrader(null);
	}

	/**
	 * Associate an edge with vertex
	 * 
	 * @param e
	 *            the edge to add (ignored if already associated)
	 */
	public void addEdge(Edge e) {
		for (int i = 0; i < 3; i++) {
			if (edge[i] == null) {
				edge[i] = e;
				return;
			} else if (edge[i] == e) {
				return;
			}
		}
	}

	/**
	 * Associate an hexagon with vertex
	 * 
	 * @param h
	 *            the hexagon to add (ignored if already associated)
	 */
	public void addHexagon(Hexagon h) {
		for (int i = 0; i < 3; i++) {
			if (hexagon[i] == null) {
				hexagon[i] = h;
				return;
			} else if (hexagon[i] == h) {
				return;
			}
		}
	}

	/**
	 * Get the hexagon at the given index
	 * 
	 * @param index
	 *            the hexagon index (0, 1, or 2)
	 * @return the hexagon or null
	 */
	public Hexagon getHexagon(int index) {
		return hexagon[index];
	}

	/**
	 * Get the hexagon's index for drawing
	 * 
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Determine if an edge is connected to vertex
	 * 
	 * @param e
	 *            the edge to check for
	 * @return true if e is connected to the vertex
	 */
	public boolean hasEdge(Edge e) {
		return (edge[0] == e || edge[1] == e || edge[2] == e);
	}

	/**
	 * Get an edge
	 * 
	 * @param index
	 *            the edge index [0, 2]
	 * @return the edge or null
	 */
	public Edge getEdge(int index) {
		return edge[index];
	}

	/**
	 * Check if vertex has a building for any player
	 * 
	 * @return true if there is a town or city for any player
	 */
	public boolean hasBuilding() {
		return (building != NONE);
	}

	/**
	 * Check if vertex has a building for a player
	 * 
	 * @param player
	 *            the player to check for
	 * @return true if player has a building on the vertexS
	 */
	public boolean hasBuilding(Player player) {
		return (owner == player);
	}

	/**
	 * Get the type of building at vertex
	 * 
	 * @return the type of building at the vertex (equal to the number of
	 *         points)
	 */
	public int getBuilding() {
		return building;
	}

	/**
	 * Get the player number of the owner of a building at vertex
	 * 
	 * @return the Player that owns it, or null
	 */
	public Player getOwner() {
		return owner;
	}

	/**
	 * Check for adjacent roads
	 * 
	 * @param player
	 *            the player to check for
	 * @return true if one of the adjacent edges has a road for player
	 */
	public boolean hasRoad(Player player) {
		for (int i = 0; i < 3; i++) {
			if (edge[i] != null && edge[i].getOwner() == player)
				return true;
		}

		return false;
	}

	public void distributeResources(Hexagon.Type type) {
		if (owner == null)
			return;

		owner.addResources(type, building);
	}

	/**
	 * Check if there are no adjacent settlements
	 * 
	 * @return true if there are no adjacent settlements
	 */
	public boolean couldBuild() {
		// check for adjacent buildings
		for (int i = 0; i < 3; i++) {
			if (edge[i] != null && edge[i].getAdjacent(this).hasBuilding())
				return false;
		}

		return true;
	}

	/**
	 * Check if player can build at vertex
	 * 
	 * @param player
	 *            player to check for
	 * @param setup
	 *            setup condition allows player to build without a road
	 * @return true if player can build at vertex
	 */
	public boolean canBuild(Player player, int type, boolean setup) {
		if (!couldBuild())
			return false;

		// only allow building towns
		if (setup)
			return (owner == null);

		// check if owner has road to vertex
		if (!this.hasRoad(player))
			return false;

		// can build town
		if (owner == null && type == TOWN)
			return true;

		// can build city
		else if (owner == player && type == CITY && building == TOWN)
			return true;

		else
			return false;
	}

	/**
	 * Simple version of canBuild(player, setup) where setup is false
	 * 
	 * @param player
	 *            player to check for
	 * @return true if player can build at vertex
	 */
	public boolean canBuild(Player player, int type) {
		return this.canBuild(player, type, false);
	}

	/**
	 * Build at vertex for player
	 * 
	 * @param player
	 *            which player intends to build
	 * @param setup
	 *            setup condition allows player to build without a road
	 */
	public boolean build(Player player, int type, boolean setup) {
		if (!this.canBuild(player, type, setup))
			return false;

		switch (building) {
		case NONE:
			owner = player;
			building = TOWN;
			break;
		case TOWN:
			building = CITY;
			break;
		case CITY:
			return false;
		}

		if (trader != null)
			player.setTradeValue(trader.getType());

		return true;
	}

	public void setTrader(Trader trader) {
		this.trader = trader;
	}

	public Trader getTrader() {
		return trader;
	}

	/**
	 * Find the longest road passing through this vertex for the given player
	 * 
	 * @param player
	 *            the player
	 * @param omit
	 *            omit an edge already considered
	 * @return the road length
	 */
	public int getRoadLength(Player player, Edge omit, int countId) {
		int longest = 0;

		// FIXME: if two road paths diverge and re-converge, the result may be
		// calculated with whichever happens to be picked first

		// another player's road breaks the road chain
		if (owner != null && owner != player)
			return 0;

		// find the longest road aside from one passing through the given edge
		for (int i = 0; i < 3; i++) {
			if (edge[i] == null || edge[i] == omit)
				continue;

			int length = edge[i].getRoadLength(player, this, countId);
			if (length > longest)
				longest = length;
		}

		return longest;
	}

	/**
	 * Initialize vertices
	 * 
	 * @return list of vertices
	 */
	public static Vertex[] initialize() {
		Vertex[] vertex = new Vertex[NUM_VERTEX];
		for (int i = 0; i < NUM_VERTEX; i++)
			vertex[i] = new Vertex(i);

		return vertex;
	}
}
