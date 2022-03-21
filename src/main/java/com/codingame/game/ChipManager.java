package com.codingame.game;

import java.util.Random;
import java.util.ArrayList;

import com.codingame.game.exception.GameException;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.google.inject.Inject;
import com.codingame.game.exception.ChipNotFoundException;
import com.codingame.game.exception.ChipNotSelectedException;

public class ChipManager {
	@Inject private MultiplayerGameManager<Player> gameManager;
	private static int index = 0;
	int remainingChips[][];
	int selectedChips[][];
	Random random;

	ChipManager() {
		this.random = new Random(gameManager.getSeed());
		this.remainingChips = new int[gameManager.getPlayerCount()][Config.COLORS_PER_PLAYER];
		this.selectedChips = new int[gameManager.getPlayerCount()][Config.COLORS_PER_PLAYER];
	}

	public boolean colorIsValid(int colorId) {
		return (colorId >= 0 && colorId < Config.COLORS_PER_PLAYER);
	}

	public Chip createChip(Player owner, int colorId, HexCoord coord) throws GameException {
		if (!colorIsValid(colorId)) {
			throw new ChipNotFoundException(colorId);
		}
		int index = owner.getIndex();
		if (selectedChips[index][colorId] == 0) {
			throw new ChipNotSelectedException(colorId);
		}
		selectedChips[index][colorId]--;

		Chip chip = new Chip(ChipManager.index++, colorId, owner, coord);
		return chip;
	}

	private ArrayList<Integer> getIndicesOfRemainingChipsForPlayer(Player player) {
		int index = player.getIndex();
		ArrayList<Integer> indices = new ArrayList<>(Config.COLORS_PER_PLAYER);
		for (int i = 0; i < remainingChips[index].length; i++) {
			if (remainingChips[index][i] != 0) {
				indices.add(i);
			}
		}
		return indices;
	}

	public void populateSelectionForPlayer(Player player) {
		int index = player.getIndex();
		for (int i = 0; i < Config.COLORS_PER_ROUND; i++) {
			ArrayList<Integer> indices = getIndicesOfRemainingChipsForPlayer(player);
			int choice = random.nextInt(indices.size());
			remainingChips[index][choice]--;
			selectedChips[index][choice]++;
		}
	}

	public void emptySelectionForPlayer(Player player) {
		int index = player.getIndex();
		for (int i = 0; i < selectedChips[index].length; i++) {
			remainingChips[index][i] += selectedChips[index][i];
			selectedChips[index][i] = 0;
		}
	}

	public void destroySelection() {
		for (int i = 0; i < selectedChips.length; i++) {
			for (int j = 0; j < selectedChips[i].length; j++) {
				selectedChips[i][j] = 0;
			}
		}
	}

}
