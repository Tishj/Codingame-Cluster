package com.codingame.game.exception;

@SuppressWarnings("serial")
public class ColumnOutOfRangeException extends GameException {
	public ColumnOutOfRangeException(int column) {
		super("Column " + column + " out of range.");
	}
}
