package com.codingame.game;

import java.util.ArrayList;

public class Connection {
	public int colorIndex;
	public int length;
	ArrayList<Chip>	chips;

	public Connection(int color) {
		this.length = 0;
		this.chips = null;
		this.colorIndex = color;
	}

	public void updateIfBigger(int color, ArrayList<Chip> chips) {
		if (chips.size() <= this.length)
			return;
		this.length = chips.size();
		this.chips = chips;
		this.colorIndex = color;
	}
}
