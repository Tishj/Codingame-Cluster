package com.codingame.game.exception;

@SuppressWarnings("serial")
public class ChipNotFoundException extends GameException {
	public ChipNotFoundException(int id) {
		super("Chip with index " + id + " was not found");
	}
}
