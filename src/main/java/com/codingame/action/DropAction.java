package com.codingame.game.action;

public class DropAction extends Action {

	public DropAction(int targetId, int colorId) {
		this.targetId = targetId;
		this.colorId = colorId;
	}

	@Override
	public boolean isDrop() {
		return true;
	}
}
