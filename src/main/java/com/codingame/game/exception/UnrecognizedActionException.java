package com.codingame.game.exception;

@SuppressWarnings("serial")
public class UnrecognizedActionException extends GameException {
	public UnrecognizedActionException() {
		super("Action played by player was not recognized");
	}
}
