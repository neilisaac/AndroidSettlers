package com.settlers.hd;

public interface AutomatedPlayer {

	/**
	 * Select the location to build a town; Note: you must build there before
	 * returning
	 * 
	 * @param vertices
	 *            the vertex list
	 * @return the index of the vertex you built on
	 */
	public int setupTown(Vertex[] vertices);

	/**
	 * Select the location to place a road; Note: you must build there before
	 * returning
	 * 
	 * @param edges
	 *            the edge list
	 * @return the index of the edge you built on
	 */
	public int setupRoad(Edge[] edges);

	/**
	 * Select the location to place a road; Note: you must build there before
	 * returning
	 * 
	 * @note in the case where a road can't be built, return -1
	 * 
	 * @param edges
	 *            the edge list
	 * @return the index of the edge you built on or -1
	 */
	public int progressRoad(Edge[] edges);

	/**
	 * Run production phase
	 */
	public void productionPhase();

	/**
	 * Run build phase
	 */
	public void buildPhase();

	/**
	 * Select a hexagon to place the robber
	 * 
	 * @param hexagons
	 *            the list of hexagons
	 * @param exception
	 *            forbidden location (where the robber came from)
	 * @return the index of the hexagon to place the robber on
	 */
	public int placeRobber(Hexagon[] hexagons, Hexagon exception);

	/**
	 * Select a player to steal from
	 * 
	 * @param players
	 *            list of players that you could steal from
	 * @return the index of the player to steal from
	 */
	public int steal(Player[] players);

	/**
	 * Consider trading "type" to "player" for "offer"
	 * 
	 * @param player
	 *            the player proposing the trade
	 * @param type
	 *            the type that the player wants
	 * @param offer
	 *            how many of each resource the player is offering
	 * @return the offer (to accept), a counter-offer, or null (to reject)
	 */
	public int[] offerTrade(Player player, Hexagon.Type type, int[] offer);

	/**
	 * Instruct the player to discard resources
	 * 
	 * @note use Player.discard() via super
	 * 
	 * @param quantity
	 *            the number of resources that must be discarded
	 */
	public void discard(int quantity);
}
