package com.codingame.game;

import java.lang.Math;

public enum Gravity {
	SOUTH(0),
	SOUTH_WEST(1),
	NORTH_WEST(2),
	NORTH(3),
	NORTH_EAST(4),
	SOUTH_EAST(5);
	
	int index;
	
	Gravity(int index) {
		this.index = index;
	}
	public int getIndex() {
		return index;
	}
	
	public Gravity opposite() {
		int index = this.getIndex();
		return Gravity.values()[index + 3 % 6];
	}
	
	public Gravity rotate(int cycles) {
		int index = this.getIndex();
		if (cycles < 0) {
			cycles = 6 - (Math.abs(cycles) % 6);
		}
		return Gravity.values()[index + cycles % 6];
	}
};
