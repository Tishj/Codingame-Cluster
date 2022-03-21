package com.codingame.game.exception;

@SuppressWarnings("serial")
public class ChipNotSelectedException extends GameException {
	public ChipNotSelectedException(int id) {
		super("Chip with index " + id + " was not selected for player");
	}
}
