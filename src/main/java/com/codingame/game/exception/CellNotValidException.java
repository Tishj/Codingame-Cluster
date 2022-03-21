package com.codingame.game.exception;

@SuppressWarnings("serial")
public class CellNotValidException extends GameException {

	public CellNotValidException(int id) {
		super("You can't place a chip on cell " + id);
	}

}
