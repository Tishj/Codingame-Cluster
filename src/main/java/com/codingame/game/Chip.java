package com.codingame.game;

public class Chip {
	int			colorId;
	Player		owner;
	int			index;
	HexCoord	coord;

	// public static final Chip NO_CHIP = new Chip(-1, -1) {

	// };

	Chip(int index, int colorId, Player owner, HexCoord coord) {
		this.index = index;
		this.colorId = colorId;
		this.owner = owner;
		this.coord = coord;
	}

	public HexCoord getCoord() {
		return coord;
	}

	public void setCoord(HexCoord newCoord) {
		this.coord = newCoord;
	}

	public int getIndex() {
		return index;
	}

	public int getColorId() {
		return colorId;
	}

	public Player getOwner() {
		return owner;
	}
}
