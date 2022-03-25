package com.codingame.game.exception;

@SuppressWarnings("serial")
public class NoChipsRemainingException extends GameException {
	public NoChipsRemainingException() {
		super("No chips are left to be picked for the player.");
	}
}
