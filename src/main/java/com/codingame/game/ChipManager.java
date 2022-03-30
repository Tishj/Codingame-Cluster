package com.codingame.game;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.codingame.gameengine.core.MultiplayerGameManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ChipManager {
	private ArrayDeque<Integer> 					indices;
	public int 										selectedAmount;
	int[][] 										remainingChips;
	int[][] 										selectedChips;
	int[][] 										placedChips;
	private int 									movedHexes = 0;
	Random											random;
	TreeMap<Integer, Chip>							chips;
	@Inject private MultiplayerGameManager<Player>	gameManager;
	Board 											board;
	
	public void init(Board board) {
		// this.gameManager = gameManager;
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

	public void removeListOfChips(HashSet<Integer> chips) {
		chips.forEach(chipIndex -> {
			removeChip(this.chips.get(chipIndex));
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

	public boolean shouldDrop(Gravity gravity) {
		for (Chip chip : chips.values()) {
			HexCoord position = chip.getCoord();
			Cell neighbour = board.map.get(position.neighbour(gravity));
			//bottom of the board reached.
			if (neighbour == null) {
				continue;
			}
			//neighbour is empty
			if (neighbour.getChip() == null) {
				return true;
			}

		}
		return false;
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

	private void updateChipLocation(Cell oldCell, Cell newCell, Chip chip, HexCoord newCoord) {
		oldCell.setChip(null);
		newCell.setChip(chip);
		chip.setCoord(newCoord);
	}

	//@return hexes moved
	public int dropChip(Chip chip, Gravity gravity, HashSet<Integer> cellIndices) {
		HexCoord	originalPosition = chip.getCoord();

		while (true) {
			HexCoord coord = chip.getCoord();
			Cell cell = board.map.get(coord);

			coord = coord.neighbour(gravity);
			Cell neighbour = board.map.get(coord);
			if (neighbour == null) {
				break;
			}
			Chip neighbourChip = neighbour.getChip();
			if (neighbourChip != null) {
				//neighbourChip hasnt moved
				if (dropChip(neighbourChip, gravity, cellIndices) == 0) {
					break;
				}
				cellIndices.add(neighbourChip.getIndex());
			}
			updateChipLocation(cell, neighbour, chip, coord);
		}
		return chip.getCoord().distanceTo(originalPosition);
	}

	public HashSet<Integer> dropChips(Gravity gravity) {
		int hexes_moved = 0;
		HashSet<Integer> cellIndices = new HashSet<>(chips.size());
		for (Chip chip: chips.values()) {
			int moved = dropChip(chip, gravity, cellIndices);
			if (moved != 0) {
				cellIndices.add(chip.getIndex());
			}
			if (moved > hexes_moved) {
				hexes_moved = moved;
			}
		}
		this.movedHexes = hexes_moved;
		return cellIndices;
	}

	public int getAndResetHexesMoved() {
		int moved = movedHexes;
		movedHexes = 0;
		return moved;
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
