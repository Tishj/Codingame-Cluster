package com.codingame.game.exception;

@SuppressWarnings("serial")
public class ColorOutOfRangeException extends GameException {
	public ColorOutOfRangeException(int color) {
		super("Color " + color + " out of range.");
	}
}
