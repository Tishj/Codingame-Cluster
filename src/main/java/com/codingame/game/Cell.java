package com.codingame.game;

public class Cell {
	public static final Cell NO_CELL = new Cell(-1, new HexCoord(0,0,0)) {
		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public int getIndex() {
			return -1;
		}
	};
	int			index;
	Chip		chip;
	HexCoord	coord;
	
	Cell(int index, HexCoord coord) {
		this.index = index;
		this.chip = null;
		this.coord = coord;
	}

	public boolean isValid() {
		return true;
	}
	public int getIndex() {
		return index;
	}
	public HexCoord getCoord() {
		return this.coord;
	}

	public Chip getChip() {
		return chip;
	}
	public void setChip(Chip chip) {
		this.chip = chip;
	}
}
