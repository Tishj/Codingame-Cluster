package com.codingame.game.exception;

@SuppressWarnings("serial")
public class ChipNotFoundException extends GameException {
	ChipNotFoundException(int id) {
		super("Chip with index " + id + " was not found");
	}
}
