package com.codingame.game;

import com.codingame.game.action.Action;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.gameengine.module.entities.Group;

public class Player extends AbstractMultiplayerPlayer {
	public Group hud;
	private Action action;
	private int[] remainingChips;
	private int[] selectedChips;

	Player() {
		initializeColors();
	}

	private void initializeColors() {
		this.remainingChips = new int[Config.COLORS_PER_PLAYER];
		this.selectedChips = new int[Config.COLORS_PER_PLAYER];
		for (int i = 0; i < Config.COLORS_PER_PLAYER; i++) {
			remainingChips[i] = Config.CHIP_MAX;
			selectedChips[i] = 0;
		}
	}

	public void endTurn() {
		for (int i = 0; i < selectedChips.length; i++) {
			selectedChips[i] = 0;
		}
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
