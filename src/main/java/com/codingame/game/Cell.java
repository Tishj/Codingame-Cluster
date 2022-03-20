package com.codingame.game;

public class Cell {
	public static final Cell NO_CELL = new Cell(-1) {
		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public int getIndex() {
			return -1;
		}
	};
	int index;
	
	Cell(int index) {
		this.index = index;
	}
	
	public boolean isValid() {
		return true;
	}
	public int getIndex() {
		return index;
	}
}
