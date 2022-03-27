package com.codingame.game;

import java.util.ArrayList;

import com.codingame.game.action.Action;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;

public class Player extends AbstractMultiplayerPlayer {
	private Action		action;
	private Connection	connection;
	
	Player() {
		this.connection = new Connection();
	}
	
	@Override
	public int getExpectedOutputLines() {
		return 1;
	}
	
	public void addScore(int score) {
		this.setScore(this.getScore() + score);
	}
	
	public void setAction(Action action) {
		this.action = action;
	}
	
	public Action getAction() {
		return action;
	}
	
	public void resetConnection() {
		connection.reset();
	}

	public void updateConnection(int color, ArrayList<Chip> chips, int amountPresentOnBoard) {
		this.connection.updateIfBigger(color, chips, amountPresentOnBoard);
	}

	public Connection getConnection() {
		return this.connection;
	}

	public String scoreToString() {
		return String.valueOf(this.getScore());
	}
}
