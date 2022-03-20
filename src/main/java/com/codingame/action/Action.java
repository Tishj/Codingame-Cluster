package com.codingame.game.action;

public abstract class Action {
	public Integer colorId;
	public Integer targetId;
	public Integer cycleAmount;

	public static final Action NO_ACTION = new Action() {
	};

	public boolean isRotate() {
		return false;
	}

	public boolean isDrop() {
		return false;
	}

	public Integer getColorId() {
		return colorId;
	}

	public Integer getCycleAmount() {
		return cycleAmount;
	}

	public Integer getTargetId() {
		return targetId;
	}
}
