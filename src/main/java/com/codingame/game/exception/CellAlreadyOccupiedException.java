package com.codingame.game.exception;

@SuppressWarnings("serial")
public class CellAlreadyOccupiedException extends GameException {
	public CellAlreadyOccupiedException(int id) {
		super("Cell with index " + id + " already contains a chip");
	}
}
