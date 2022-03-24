package com.codingame.game;

import java.util.ArrayDeque;
import java.util.HashSet;

import com.codingame.game.action.Action;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.gameengine.module.entities.Group;

public class Player extends AbstractMultiplayerPlayer {
	public Group hud;
	private Action action;
	ArrayDeque<Chip>	unknown;
	HashSet<Chip>	changed;

	Player() {
		unknown = new ArrayDeque<Chip>();
		changed = new HashSet<Chip>();
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

	public void addToUnknown(Chip chip) {
		unknown.push(chip);
	}

	public Chip[] getUnknown() {
		Chip[] chips = unknown.toArray(Chip[]::new);
		unknown.clear();
		return chips;
	}

	public void addToChanged(Chip chip) {
		changed.add(chip);
	}

	public Chip[] getChanged() {
		Chip[] chips = changed.toArray(Chip[]::new);
		changed.clear();
		return chips;
	}
}
