package com.codingame.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

import com.codingame.game.action.Action;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.gameengine.module.entities.Group;

public class Player extends AbstractMultiplayerPlayer {
	public Group hud;
	private Action action;
	ArrayDeque<Chip>	unknown;
	HashSet<HexCoord>	changed;

	Player() {
		unknown = new ArrayDeque<Chip>();
		changed = new HashSet<HexCoord>();
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
		Chip[] chips = unknown.toArray(new Chip[unknown.size()]);
		unknown.clear();
		return chips;
	}

	public void addToChanged(HexCoord coord) {
		changed.add(coord);
	}

	public String scoreToString() {
		return String.valueOf(this.getScore());
	}

	public HashSet<HexCoord> getChanged() {
		return changed;
	}
	public void clearChanged() {
		changed.clear();
	}
}
