package com.codingame.game;

public class Chip {
	int			colorId;
	Player		owner;
	int			index;
	HexCoord	coord;

	@Override
	public int hashCode() {
		return index;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)  {
			return false;
		}
		if (this == o) {
			return true;
		}
		if (o instanceof Chip == false) {
			return false;
		}
		Chip other = (Chip)o;
		if (other.colorId != this.colorId) {
			return false;
		}
		if (other.owner != this.owner) {
			return false;
		}
		if (other.index != this.index) {
			return false;
		}
		if (other.coord != this.coord) {
			return false;
		}
		return true;
	}

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
