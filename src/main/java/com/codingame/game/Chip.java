package com.codingame.game;

public class Chip {
	int		colorId;
	Player	owner;
	int		index;

	public static final Chip NO_CHIP = new Chip(-1) {
	};

	Chip(int index, int colorId) {
		this.index = index;
		this.colorId = colorId;
	}

	public int getIndex() {
		return index;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public int getColorId() {
		return colorId;
	}

	public Player getOwner() {
		return owner;
	}
}
