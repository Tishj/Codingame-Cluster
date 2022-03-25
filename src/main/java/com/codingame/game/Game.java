
package com.codingame.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codingame.game.action.Action;
import com.codingame.game.exception.CellAlreadyOccupiedException;
import com.codingame.game.exception.CellNotFoundException;
import com.codingame.game.exception.CellNotValidException;
import com.codingame.game.exception.UnrecognizedActionException;
import com.codingame.game.exception.GameException;
import com.codingame.game.exception.NoChipsRemainingException;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Game {

	@Inject private MultiplayerGameManager<Player> gameManager;
	@Inject private GameSummaryManager gameSummaryManager;
	@Inject private ChipManager chipManager;

	public static int MAX_ROUNDS = 30;

	Board		board;
	List<Chip>	placedChips;
	Gravity		gravity;
	Random		random;
	int			round = 0;
	int			turn = 0;

	public void init(long seed) {

		MAX_ROUNDS = Config.MAX_ROUNDS;

		random = new Random(seed);
		board = BoardGenerator.generate(random);
		chipManager.init(gameManager);
		this.gravity = Gravity.SOUTH;
		this.placedChips = new ArrayList<>(Config.CELL_COUNT);

		round = 0;
	}

	public static String getExpected() {
		return "ROTATE <amount> | DROP <row> <color_idx>";
	}

	private HexCoord getCoordByIndex(int index) throws CellNotFoundException {
		return board.map.entrySet()
			.stream()
			.filter(e -> e.getValue().getIndex() == index)
			.findFirst()
			.orElseThrow(() -> {
				return new CellNotFoundException(index);
			}).getKey();
	}

	private List<HexCoord> getBoardEdges() {
		HexCoord centre = new HexCoord(0, 0, 0);
		return board.coords.stream()
			.filter(coord -> coord.distanceTo(centre) == Config.MAP_RING_COUNT)
			.collect(Collectors.toList());
	}

	public List<String> getCurrentFrameInfoFor(Player player) {
		List<String> lines = new ArrayList<>();
		lines.add(String.valueOf(gravity.getIndex()));

		int amountOfColumns = Config.COLUMN_COUNT;

		List<Integer> columns = new ArrayList<>(amountOfColumns);
		for (int i = 0; i < amountOfColumns; i++) {
			Cell cell = board.map.get(board.getTopOfColumn(gravity, i));
			assert(cell != null);
			Chip chip = cell.getChip();
			if (chip != null) {
				columns.add(i);
			}
		}
		//invalidColumns
		lines.add(String.valueOf(columns.size()));
		for (int i = columns.size() - 1; i >= 0; i--) {
			lines.add(String.valueOf(columns.get(i)));
		}

		//newChips
		Chip[] newChips = player.getUnknown();
		lines.add(String.valueOf(newChips.length));
		for (Chip chip : (Chip[])newChips) {
			lines.add(String.format("%d %d %d %d",
				chip.getIndex(),
				chip.getColorId(),
				board.map.get(chip.getCoord()).getIndex(),
				chip.getOwner().getIndex() == player.getIndex() ? 1 : 0
				));
		}

		//changedChips
		Chip[] changedChips = player.getChanged();
		lines.add(String.valueOf(changedChips.length));
		for (Chip chip : (Chip[])changedChips) {
			lines.add(String.format("%d %d",
				chip.getIndex(),
				board.map.get(chip.getCoord()).getIndex()
			));
		}

		//selectedColors
		int amountOfSelectedColors = chipManager.selectedAmount;
		lines.add(String.valueOf(amountOfSelectedColors));
		int[] selectedColors = new int[amountOfSelectedColors];
		int index = 0;
		for (int i = 0; i < Config.COLORS_PER_PLAYER; i++) {
			for (int j = 0; j < chipManager.selectedChips[player.getIndex()][i]; j++) {
				selectedColors[index++] = i;
			}
		}
		for (int color : selectedColors) {
			lines.add(String.valueOf(color));
		}

		return lines;
	}

	private HexCoord hexAdd(HexCoord a, HexCoord b) {
		return new HexCoord(a.getQ() + b.getQ(), a.getR() + b.getR(), a.getS() + b.getS());
	}

	private List<HexCoord> getCoordsInRange(HexCoord center, int N) {
		List<HexCoord> results = new ArrayList<>();
		for (int x = -N; x <= +N; x++) {
			for (int y = Math.max(-N, -x - N); y <= Math.min(+N, -x + N); y++) {
				int z = -x - y;
				results.add(hexAdd(center, new HexCoord(x, y, z)));
			}
		}
		return results;
	}
	// Slot* neighbour = pellet->slot->neighbours[direction];

	// if (!neighbour || !neighbour->pellet || neighbour->pellet->color != pellet->color)
	// 	return info;
	// info.size += 1;
	// return match_size(neighbour->pellet, info, direction);
	private int getMatchLength(Chip chip, Gravity direction, int size) {
		HexCoord coord = chip.coord.neighbour(direction);
		Cell cell = getBoard().get(coord);
		if (cell == null)
			return size;
		Chip other = cell.getChip();
		if (other == null || (other.colorId != chip.colorId || other.owner.getIndex() != chip.owner.getIndex()))
			return size;
		return getMatchLength(other, direction, size + 1);
	}

	public GameResult getWinner() {
		if (placedChips.size() < Config.WIN_LENGTH) {
			return GameResult.IN_PROGRESS;
		}
		Connection[] biggestConnection = new Connection[gameManager.getPlayerCount()];
		for (int i = 0; i < gameManager.getPlayerCount(); i++) {
			biggestConnection[i] = new Connection(-1, 0);
		}
		//check match lengths
		for (Chip chip : placedChips) {
			for (Gravity direction : Gravity.values()) {
				int length = getMatchLength(chip, direction, 1);
				biggestConnection[chip.getOwner().getIndex()].updateIfBigger(chip.colorId, length);
			}
		}
		gameManager.addToGameSummary("Red: length: " + biggestConnection[0].length);
		gameManager.addToGameSummary("Blue:  length: " + biggestConnection[1].length);
		//No winner
		if (biggestConnection[0].length < Config.WIN_LENGTH && biggestConnection[1].length < Config.WIN_LENGTH) {
			return GameResult.IN_PROGRESS;
		}
		//Blue wins
		if (biggestConnection[0].length > biggestConnection[1].length) {
			return GameResult.WIN_PLAYER_TWO;
		}
		//Red wins
		if (biggestConnection[1].length > biggestConnection[0].length) {
			return GameResult.WIN_PLAYER_ONE;
		}
		//Possible tie
		long amountOfBlueChips = placedChips.stream()
			.filter(c -> 
				(c.colorId == biggestConnection[1].colorIndex &&
				c.getOwner().getIndex() == 1))
			.count();
		long amountOfRedChips = placedChips.stream()
			.filter(c -> 
				(c.colorId == biggestConnection[0].colorIndex &&
				c.getOwner().getIndex() == 0))
			.count();
		if (amountOfBlueChips > amountOfRedChips) {
			return GameResult.WIN_PLAYER_TWO;
		}
		if (amountOfRedChips > amountOfBlueChips) {
			return GameResult.WIN_PLAYER_ONE;
		}
		return GameResult.TIE;
	}

	public List<String> getGlobalInfoFor(Player player) {
		List<String> lines = new ArrayList<>();
		lines.add(String.valueOf(board.coords.size()));
		board.coords.forEach(coord -> {
			Cell cell = board.map.get(coord);
			lines.add(
				String.format(
					"%d %s",
					cell.getIndex(),
					getNeighbourIds(coord)
				)
			);
		});
		int amountOfColumns = Config.COLUMN_COUNT;
		lines.add(String.valueOf(amountOfColumns));
		for (int i = 0; i < amountOfColumns; i++) {
			lines.add(getInsertColumnCellIndices(i));
		}

		//yourColors
		lines.add(String.valueOf(Config.COLORS_PER_PLAYER));
		for (int i = 0; i < Config.COLORS_PER_PLAYER; i++) {
			lines.add(String.valueOf(Config.CHIP_MAX));
		}

		//opponentColors
		lines.add(String.valueOf(Config.COLORS_PER_PLAYER));
		for (int i = 0; i < Config.COLORS_PER_PLAYER; i++) {
			lines.add(String.valueOf(Config.CHIP_MAX));
		}

		return lines;
	}

	private String getInsertColumnCellIndices(int column) {
		List<Integer>	indices = new ArrayList<>(6);
		for (Gravity direction : Gravity.values()) {
			indices.add(
				board.map.getOrDefault(board.getTopOfColumn(direction, column), Cell.NO_CELL).getIndex()
			);
		}
		return indices.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(" "));
	}

	private String getNeighbourIds(HexCoord coord) {
		List<Integer> orderedneighbourIds = new ArrayList<>(HexCoord.directions.length);
		for (int i = 0; i < HexCoord.directions.length; ++i) {
			orderedneighbourIds.add(
				board.map.getOrDefault(coord.neighbour(i), Cell.NO_CELL).getIndex()
			);
		}
		return orderedneighbourIds.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(" "));
	}

	public void resetGameTurnData() {
	}

	private Chip doDrop(Player player, Action action) throws GameException {
		HexCoord startingLocation = board.getTopOfColumn(gravity, action.targetId);
		assert(startingLocation != null);
		Cell cell = getBoard().get(startingLocation);
		if (cell == null || cell.getChip() != null) {
			throw new CellAlreadyOccupiedException(cell.getIndex());
		}
		//create the chip
		Chip chip = chipManager.createChip(player, action.colorId, startingLocation);
		Player other = gameManager.getPlayer(1 - player.getIndex());
		player.addToUnknown(chip);
		other.addToUnknown(chip);
		//add it to the chips list
		placedChips.add(chip);
		cell = getBoard().get(chip.getCoord());
		cell.setChip(chip);
		
		dropChip(chip, null);

		//move rest of selection back to remaining
		chipManager.emptySelectionForPlayer(player);
		return chip;
	}

	private void doRotate(Player player, Action action) throws GameException {
		gravity = gravity.rotate(action.cycleAmount);

		//Drop all chips
		ArrayList<Chip> changedChips = new ArrayList<>(placedChips.size());
		for (Chip chip: placedChips) {
			if (dropChip(chip, changedChips)) {
				changedChips.add(chip);
			}
		}
		//Add the changed chips to the players(and other) changed chips
		Player other = gameManager.getPlayer(1 - player.getIndex());
		for (Chip chip : changedChips) {
			other.addToChanged(chip);
			player.addToChanged(chip);
		}
		//Burn the selected chips
		chipManager.destroySelection();
	}

	private void updateChipLocation(Cell oldCell, Cell newCell, Chip chip, HexCoord newCoord) {
		oldCell.setChip(null);
		newCell.setChip(chip);
		chip.setCoord(newCoord);
	}

	//@return true/false indicating whether the chip moved at least one place or not
	public boolean dropChip(Chip chip, ArrayList<Chip> changedChips) {
		Map<HexCoord, Cell> board = getBoard();
		//Move it downwards
		boolean moved = false;
		while (true) {
			//Current coordinate of the chip
			HexCoord coord = chip.getCoord();
			//Get the cell corresponding to that coordinate
			Cell cell = board.get(coord);

			//Get the neighbour
			coord = coord.neighbour(gravity);
			//Get the cell of the neighbour
			Cell neighbour = board.get(coord);
			//If we've reached the bottom of the board, we're done
			if (neighbour == null) {
				break;
			}
			//Check if the neighbour already contains a chip
			Chip neighbourChip = neighbour.getChip();
			//Cell is not vacant
			if (neighbourChip != null) {
				if (!dropChip(neighbourChip, changedChips)) {
					break;
				}
				//Chip moved, cell is now vacant
				//Optional
				if (changedChips != null) {
					changedChips.add(neighbourChip);
				}
			}
			updateChipLocation(cell, neighbour, chip, coord);
			moved = true;
		}
		return moved;
	}

	public void preparePlayerDataForRound(Player player) throws GameException {
		if (!chipManager.populateSelectionForPlayer(player)) {
			throw new NoChipsRemainingException();
		}
	}

	public void performGameUpdate(Player player) {
		gameManager.addToGameSummary("Round:" + round );
		turn++;
		round++;

		performActionUpdate(player);

		gameManager.addToGameSummary(gameSummaryManager.toString());
		gameSummaryManager.clear();

		if (gameOver()) {
			gameManager.endGame();
		} else {
			gameManager.setMaxTurns(turn + 1);
		}
	}

	public void performActionUpdate(Player player) {
		try {
			Action action = player.getAction();
			if (action.isDrop()) {
				//Keep track of the chip so we know which one was added this turn
				Chip chip = doDrop(player, action);
			} else if (action.isRotate()) {
				doRotate(player, action);
			}
			else {
				//throw exception
				throw new UnrecognizedActionException();
			}
		} catch (GameException e) {
			gameSummaryManager.addError(player.getNicknameToken() + ": " + e.getMessage());
			gameManager.getPlayer(1 - player.getIndex()).setScore(100);
			player.setScore(-1);
			gameManager.endGame();
			return;
		}
		gameManager.setFrameDuration(1000);
	}

	public Map<HexCoord, Cell> getBoard() {
		return board.map;
	}

	private boolean gameOver() {
		return gameManager.getActivePlayers().size() <= 1 || round >= Config.MAX_ROUNDS;
	}

	public int getRound() {
		return round;
	}

	public int getTurn() {
		return turn;
	}

	public Gravity getGravity() {
		return gravity;
	}

	public List<Chip> getChips() {
		return this.placedChips;
	}

}
