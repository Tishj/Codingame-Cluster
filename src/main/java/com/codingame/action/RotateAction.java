package com.codingame.game.action;

public class RotateAction extends Action {

	public RotateAction(int cycleAmount) {
		this.cycleAmount = cycleAmount;
	}

	@Override
	public boolean isRotate() {
		return true;
	}
}
