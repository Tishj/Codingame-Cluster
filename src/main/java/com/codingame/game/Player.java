package com.codingame.game;

import com.codingame.game.action.Action;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;

public class Player extends AbstractMultiplayerPlayer {
	private Action action;
	
	Player() {
	}
	
	@Override
	public int getExpectedOutputLines() {
		return 1;
	}
	
	public void addScore(int score) {
		setScore(getScore() + score);
	}
	
	public void setAction(Action action) {
		this.action = action;
	}
	
	public Action getAction() {
		return action;
	}
	
	public String scoreToString() {
		return String.valueOf(this.getScore());
	}
}
