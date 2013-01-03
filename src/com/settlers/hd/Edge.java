package com.settlers.hd;

public class Edge {

	public static final int NUM_EDGES = 72;

	private int index;
	private Vertex[] vertex;
	private Player owner;
	private int lastRoadCountId;

	/**
	 * Initialize edge with vertices set to null
	 */
	public Edge(int index) {
		this.index = index;
		vertex = new Vertex[2];
		vertex[0] = vertex[1] = null;
		owner = null;
		lastRoadCountId = 0;
	}

	/**
	 * Set vertices for the edge
	 * 
	 * @param v1
	 *            the first vertex
	 * @param v2
	 *            the second vertex
	 */
	public void setVertices(Vertex v1, Vertex v2) {
		vertex[0] = v1;
		vertex[1] = v2;
		v1.addEdge(this);
		v2.addEdge(this);
	}

	/**
	 * Check if the edge has a given vertex
	 * 
	 * @param v
	 *            the vertex to check for
	 * @return true if v is associated with the edge
	 */
	public boolean hasVertex(Vertex v) {
		return (vertex[0] == v || vertex[1] == v);
	}

	/**
	 * Get the other vertex associated with edge
	 * 
	 * @param v
	 *            one vertex
	 * @return the other associated vertex or null if not completed
	 */
	public Vertex getAdjacent(Vertex v) {
		if (vertex[0] == v)
			return vertex[1];
		else if (vertex[1] == v)
			return vertex[0];

		return null;
	}

	/**
	 * Check if a road has been build at the edge
	 * 
	 * @return true if a road was built
	 */
	public boolean hasRoad() {
		return (owner != null);
	}

	/**
	 * Get the owner's player number
	 * 
	 * @return 0 or the owner's player number
	 */
	public Player getOwner() {
		return owner;
	}

	/**
	 * Determine if player can build a road on edge
	 * 
	 * @param player
	 *            the player to check for
	 * @return true if player can build a road on edge
	 */
	public boolean canBuild(Player player) {
		if (owner != null)
			return false;

		// check for roads to each vertex
		for (int i = 0; i < 2; i++) {
			// the player has a road to an unoccupied vertex,
			// or the player has an adjacent building
			if (vertex[i].hasRoad(player) && !vertex[i].hasBuilding()
					|| vertex[i].hasBuilding(player))
				return true;
		}

		return false;
	}

	/**
	 * Get the first vertex
	 * 
	 * @return the first vertex
	 */
	public Vertex getVertex1() {
		return vertex[0];
	}

	/**
	 * Get the second vertex
	 * 
	 * @return the second vertex
	 */
	public Vertex getVertex2() {
		return vertex[1];
	}

	/**
	 * Get the index of this edge
	 * 
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Build a road on edge
	 * 
	 * @param player
	 *            the road owner
	 * @return true if player can build a road on edge
	 */
	public boolean build(Player player) {
		if (!canBuild(player))
			return false;

		owner = player;
		return true;
	}

	/**
	 * Get the road length through this edge
	 * 
	 * @param player
	 *            player to measure for
	 * @param from
	 *            where we are measuring from
	 * @param countId
	 *            unique id for this count iteration
	 * @return the road length
	 */
	public int getRoadLength(Player player, Vertex from, int countId) {
		if (owner != player || lastRoadCountId == countId)
			return 0;

		// this ensures that that road isn't counted multiple times (cycles)
		lastRoadCountId = countId;

		// find other vertex
		Vertex to = (from == vertex[0] ? vertex[1] : vertex[0]);

		// return road length
		return to.getRoadLength(player, this, countId) + 1;
	}

	/**
	 * Get the longest road length through this edge
	 * 
	 * @param countId
	 *            unique id for this count iteration
	 * @return the road length
	 */
	public int getRoadLength(int countId) {
		if (owner == null)
			return 0;

		// this ensures that that road isn't counted multiple times (cycles)
		lastRoadCountId = countId;

		int length1 = vertex[0].getRoadLength(owner, this, countId);
		int length2 = vertex[1].getRoadLength(owner, this, countId);
		return length1 + length2 + 1;
	}

	/**
	 * Initialize the edges
	 * 
	 * @return the array of edges
	 */
	public static Edge[] initialize() {
		Edge[] edge = new Edge[NUM_EDGES];
		for (int i = 0; i < NUM_EDGES; i++)
			edge[i] = new Edge(i);

		return edge;
	}
}
