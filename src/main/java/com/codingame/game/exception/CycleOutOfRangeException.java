package com.codingame.game.exception;

@SuppressWarnings("serial")
public class CycleOutOfRangeException extends GameException {
	public CycleOutOfRangeException(int cycle) {
		super("Cycle " + cycle + " out of range.");
	}
}
