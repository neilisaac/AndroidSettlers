package com.settlers.hd;

import com.settlers.hd.Hexagon.Type;

public class Trader {

	public static final int NUM_TRADER = 9;
	
	public enum Position {
		NORTH, SOUTH, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST
	}
	
	private static final Position[] POSITION_LIST = {
		Position.NORTH, Position.NORTHWEST, Position.NORTHEAST, 
		Position.NORTHWEST, Position.NORTHEAST, Position.SOUTHWEST, 
		Position.SOUTHEAST, Position.SOUTH, Position.SOUTH 
	};

	private Type type;
	private Position position;
	private int index;

	public Trader(Type type, int index) {
		this.type = type;
		this.index = index;
		position = POSITION_LIST[index];
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public int getIndex() {
		return index;
	}
	
	public static Trader[] initialize() {

		// mark all traders as unassigned
		Trader[] trader = new Trader[NUM_TRADER];
		boolean[] usedTrader = new boolean[NUM_TRADER];
		for (int i = 0; i < NUM_TRADER; i++)
			usedTrader[i] = false;

		// for each trader type (one of each resource, 4 any 3:1 traders)
		for (int i = 0; i < NUM_TRADER; i++) {
			while (true) {
				// pick a random unassigned trader
				int pick = (int) (Math.random() * NUM_TRADER);
				if (!usedTrader[pick]) {
					Type type;
					if (i >= Hexagon.TYPES.length)
						type = Type.ANY;
					else
						type = Type.values()[i];

					trader[pick] = new Trader(type, pick);
					usedTrader[pick] = true;
					break;
				}
			}
		}
		
		return trader;
	}
	
	public static Trader[] initialize(Type[] types) {
		Trader[] trader = new Trader[NUM_TRADER];
		for (int i = 0; i < trader.length; i++)
			trader[i] = new Trader(types[i], i);
		
		return trader;
	}
}
