package com.codingame.game;

import com.codingame.game.action.Action;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.gameengine.module.entities.Group;

public class Player extends AbstractMultiplayerPlayer {
	public Group hud;
	private Action action;

	Player() {
	}

	@Override
	public int getExpectedOutputLines() {
		return 1;
	}
	
	public void setAction(Action action) {
		this.action = action;
	}

	public Action getAction() {
		return action;
	}
}
