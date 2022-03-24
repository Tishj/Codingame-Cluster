package com.codingame.game;

public class Connection {
	public int playerIndex;
	public int colorIndex;
	public int length;

	public Connection(int color, int length) {
		this.length = length;
		this.colorIndex = color;
	}

	public void updateIfBigger(int color, int length) {
		if (this.length >= length)
			return;
		this.length = length;
		this.colorIndex = color;
	}
}
