package com.codingame.game;

import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.ArrayDeque;

import com.codingame.gameengine.core.MultiplayerGameManager;
import com.google.inject.Singleton;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Singleton
public class ChipManager {
	private ArrayDeque<Integer> indices;
	public int selectedAmount;
	int[][] remainingChips;
	int[][] selectedChips;
	int[][] placedChips;
	Random	random;
	TreeMap<Integer, Chip>	chips;
	private MultiplayerGameManager<Player> gameManager;
	Board board;
	
	public void init(MultiplayerGameManager<Player> gameManager, Board board) {
		this.gameManager = gameManager;
		this.random = new Random(gameManager.getSeed());
		this.remainingChips = new int[gameManager.getPlayerCount()][Config.COLORS_PER_PLAYER];
		this.selectedChips = new int[gameManager.getPlayerCount()][Config.COLORS_PER_PLAYER];
		this.placedChips = new int[gameManager.getPlayerCount()][Config.COLORS_PER_PLAYER];

		this.board = board;

		this.chips = new TreeMap<Integer, Chip>();
		for (int[] chips : this.remainingChips) {
			Arrays.fill(chips, Config.CHIP_MAX);
		}
		indices = new ArrayDeque<>(Config.CELL_COUNT);
		for (int i = 0; i < Config.CELL_COUNT; i++) {
			indices.add(i);
		}
	}

	public boolean colorIsValid(int colorId) {
		return (colorId >= 0 && colorId < Config.COLORS_PER_PLAYER);
	}

	public Chip createChip(Player owner, int colorId, HexCoord coord) {
		int player = owner.getIndex();
		selectedChips[player][colorId]--;
		placedChips[player][colorId]++;

		int index = this.indices.removeFirst();
		Chip chip = new Chip(index, colorId, owner, coord);
		chips.put(index, chip);
		return chip;
	}

	public int getAmountPresentOnBoard(Player player, int color) {
		return this.placedChips[player.getIndex()][color];
	}

	public Map<Integer, Chip> getChips() {
		return chips;
	}

	public void removeChip(Chip chip) {
		int player = chip.getOwner().getIndex();
		int colorId = chip.getColorId();
		int index = chip.getIndex();
		//Refund the chip to the bag
		board.map.get(chip.getCoord()).setChip(null);
		placedChips[player][colorId]--;
		remainingChips[player][colorId]++;
		//Add the index to the back of the queue
		indices.add(index);
		//Remove the chip from the map
		chips.remove(index);
	}

	public void removeListOfChips(List<Chip> chips) {
		chips.forEach(chip -> {
			removeChip(chip);
		});
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
		gameManager.addToGameSummary("Chips selected for " + player.getNicknameToken() + ":");
		for (int i = 0; i < Config.COLORS_PER_PLAYER; i++) {
			gameManager.addToGameSummary(i + ": " + selectedChips[index][i]);
		}
		gameManager.addToGameSummary("Chips remaining for " + player.getNicknameToken() + ":");
		for (int i = 0; i < Config.COLORS_PER_PLAYER; i++) {
			gameManager.addToGameSummary(i + ": " + remainingChips[index][i]);
		}
		this.selectedAmount = selected;
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
