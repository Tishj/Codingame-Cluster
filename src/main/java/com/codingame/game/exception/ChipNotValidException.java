package com.codingame.game.exception;

@SuppressWarnings("serial")
public class ChipNotValidException extends GameException {
	public ChipNotValidException(int id) {
		super("Chip with index " + id + " was not selected for the player");
	}
}
