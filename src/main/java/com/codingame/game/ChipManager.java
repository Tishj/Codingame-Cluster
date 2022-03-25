package com.codingame.game;

import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.ArrayList;

import com.codingame.gameengine.core.MultiplayerGameManager;
import com.google.inject.Singleton;

import java.util.Arrays;
import java.util.List;

@Singleton
public class ChipManager {
	private static int index = 0;
	int[][] remainingChips;
	int[][] selectedChips;
	Random	random;
	
	public void init(MultiplayerGameManager<Player> gameManager) {
		this.random = new Random(gameManager.getSeed());
		this.remainingChips = new int[gameManager.getPlayerCount()][Config.COLORS_PER_PLAYER];
		this.selectedChips = new int[gameManager.getPlayerCount()][Config.COLORS_PER_PLAYER];
		for (int[] chips : this.remainingChips) {
			Arrays.fill(chips, Config.CHIP_MAX);
		}
	}

	public boolean colorIsValid(int colorId) {
		return (colorId >= 0 && colorId < Config.COLORS_PER_PLAYER);
	}

	public Chip createChip(Player owner, int colorId, HexCoord coord) {
		int index = owner.getIndex();
		selectedChips[index][colorId]--;

		return new Chip(ChipManager.index++, colorId, owner, coord);
	}

	public boolean colorIsSelected(Player player, int colorId) {
		int index = player.getIndex();
		return (selectedChips[index][colorId] != 0);
	}

	private List<Integer> getIndicesOfRemainingChipsForPlayer(Player player) {
		int index = player.getIndex();
		List<Integer> indices = IntStream.range(0,remainingChips[index].length).boxed().collect(Collectors.toList());
		for (int i = remainingChips[index].length - 1; i >= 0; i--) {
			if (remainingChips[index][i] == 0) {
				indices.remove(i);
			}
		}
		return indices;
	}

	public boolean populateSelectionForPlayer(Player player) {
		int index = player.getIndex();
		int selected = 0;
		for (int i = 0; i < Config.COLORS_PER_ROUND; i++) {
			List<Integer> indices = getIndicesOfRemainingChipsForPlayer(player);
			int remainingChipTypes = indices.size();
			if (remainingChipTypes == 0) {
				break;
			}
			int choice = random.nextInt(remainingChipTypes);
			choice = indices.get(choice); //retrieve the corresponding index

			remainingChips[index][choice]--;
			selectedChips[index][choice]++;
			selected++;
		}
		return selected != 0;
	}

	public void emptySelectionForPlayer(Player player) {
		int index = player.getIndex();
		for (int i = 0; i < selectedChips[index].length; i++) {
			remainingChips[index][i] += selectedChips[index][i];
			selectedChips[index][i] = 0;
		}
	}

	public void destroySelection() {
		for (int[] selectedChip : selectedChips) {
			Arrays.fill(selectedChip, 0);
		}
	}
}
