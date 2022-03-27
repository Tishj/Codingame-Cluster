package com.codingame.game;

import java.util.ArrayList;

public class Connection {
	public int colorIndex;
	public int length;
	public int amountPresentOnBoard;
	ArrayList<Chip>	chips;

	public Connection() {
		this.length = 0;
		this.chips = null;
		this.colorIndex = -1;
		this.amountPresentOnBoard = 0;
	}

	public void reset() {
		this.length = 0;
		if (this.chips != null)
			this.chips.clear();
		this.colorIndex = -1;
	}

	public void updateIfBigger(int color, ArrayList<Chip> chips, int amountPresentOnBoard) {
		if (chips.size() < this.length)
			return;
		if (chips.size() == this.length) {
			if (color == this.colorIndex)
				return;
			if (amountPresentOnBoard <= this.amountPresentOnBoard)
				return;
		}
		this.length = chips.size();
		this.chips = chips;
		this.colorIndex = color;
		this.amountPresentOnBoard = amountPresentOnBoard;
	}
}
