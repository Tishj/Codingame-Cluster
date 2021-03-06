
package com.codingame.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
	Gravity		gravity;
	Random		random;
	int			round = 0;
	int			turn = 0;
	FrameType	currentFrameType = FrameType.ACTIONS;
	FrameType	nextFrameType;
	Cell[][] insertionPositions;

	public void setInsertionPositions() {
		this.insertionPositions = new Cell[Config.CELL_COUNT][6];
		int index = 0;
		for (Cell[] position : this.insertionPositions) {
			for (Gravity direction : Gravity.values()) {
				position[direction.getIndex()] = board.map.get(board.getTopOfColumn(direction, index));
			}
			index++;
		}
	}

	public void init(long seed) {

		MAX_ROUNDS = Config.MAX_ROUNDS;

		random = new Random(seed);
		board = BoardGenerator.generate(random);
		chipManager.init(gameManager, board);
		this.gravity = Gravity.SOUTH;
		this.setInsertionPositions();

		round = 0;
	}

	public FrameType getCurrentFrameType() {
		return this.currentFrameType;
	}

	public static String getExpected() {
		return "ROTATE <amount> | DROP <row> <color_idx>";
	}

	public List<String> getCurrentFrameInfoFor(Player player) {
		List<String> lines = new ArrayList<>();
		lines.add(String.valueOf(gravity.getIndex())); //gravity

		int amountOfColumns = Config.COLUMN_COUNT;

		List<Integer> columns = new ArrayList<>(amountOfColumns);
		for (int i = 0; i < amountOfColumns; i++) {
			Cell cell = insertionPositions[i][gravity.getIndex()];
			Chip chip = cell.getChip();
			if (chip == null) {
				columns.add(i);
			}
		}

		lines.add(String.valueOf(columns.size())); //numberOfValidColumns
		for (int idx : columns) {
			lines.add(String.valueOf(idx)); //columnIndex
		}

		Map<Integer, Chip>	chips = chipManager.getChips();
		lines.add(String.valueOf(chips.size())); //numberOfChips
		chips.values().stream()
			.forEach(chip -> {
				int cellIndex = getBoard().get(chip.getCoord()).getIndex();
				lines.add(String.format("%d %d %d %d",
					chip.getIndex(), //index
					chip.getColorId(), //colorIndex
					chip.getOwner().getIndex() == player.getIndex() ? 1 : 0, //isMine
					cellIndex //cellIndex
				));
			});

		//selectedColors
		int amountOfSelectedColors = chipManager.selectedAmount;
		lines.add(String.valueOf(amountOfSelectedColors)); //numberOfColorsInHand
		int[] selectedColors = new int[amountOfSelectedColors];
		int index = 0;
		for (int i = 0; i < Config.COLORS_PER_PLAYER; i++) {
			for (int j = 0; j < chipManager.selectedChips[player.getIndex()][i]; j++) {
				selectedColors[index++] = i;
			}
		}
		for (int color : selectedColors) {
			lines.add(String.valueOf(color)); //colorIndex
		}

		return lines;
	}

	public Player getCurrentPlayer() {
		return this.gameManager.getPlayer(round % 2);
	}

	public boolean setGameTurnData() {
		if (round != 0) {
			currentFrameType = nextFrameType;
		}
		if (this.currentFrameType != FrameType.ACTIONS) {
			return true;
		}
		Player player = getCurrentPlayer();
		return chipManager.populateSelectionForPlayer(player);
	}

	private List<Chip> getMatchLength(Chip chip, Gravity direction, List<Chip> connection) {
		HexCoord coord = chip.coord.neighbour(direction);
		Cell cell = getBoard().get(coord);
		if (cell == null)
			return connection;
		Chip other = cell.getChip();
		if (other == null || (other.colorId != chip.colorId || other.owner.getIndex() != chip.owner.getIndex()))
			return connection;
		connection.add(other);
		return getMatchLength(other, direction, connection);
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
			lines.add(Stream.of(insertionPositions[i])
				.map(cell -> {
					return String.valueOf(cell.getIndex());
				})
				.collect(Collectors.joining(" "))
			);
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

	private Chip doDrop(Player player, Action action) {
		Cell cell = insertionPositions[action.targetId][gravity.getIndex()];
		//create the chip
		Chip chip = chipManager.createChip(player, action.colorId, cell.getCoord());
		cell = getBoard().get(chip.getCoord());
		cell.setChip(chip);

		//move rest of selection back to remaining
		chipManager.emptySelectionForPlayer(player);
		return chip;
	}

	private void doRotate(Player player, Action action) {
		gravity = gravity.rotate(action.cycleAmount);
		//Burn the selected chips
		chipManager.destroySelection();
	}

	//TODO: fix how shitty this code is..
	public boolean updateConnections() {
		Map<Integer, Chip> chips = chipManager.getChips();
		if (chips.size() < Config.WIN_LENGTH) {
			return false;
		}
		//updateConnections
		for (Chip chip : chips.values()) {
			for (Gravity direction : Gravity.values()) {
				ArrayList<Chip> connection = new ArrayList<Chip>(Config.WIN_LENGTH);
				connection.add(chip);
				getMatchLength(chip, direction, connection);
				Player player = chip.getOwner();
				int amountPresentOnBoard = chipManager.getAmountPresentOnBoard(player, chip.getColorId());
				player.updateConnection(chip.colorId, connection, amountPresentOnBoard);
			}
		}
		//If any connection is big enough to form a complete connection, return true
		for (Player player : gameManager.getActivePlayers()) {
			if (player.getConnection().length >= Config.WIN_LENGTH)
				return true;
		}
		return false;
	}

	public void removeCompleteConnection(Player player) {
		ArrayList<Chip> chips = player.getConnection().chips;
		int points = 0;
		for (Chip chip : chips) {
			points += Constants.CHIP_VALUE;
			chipManager.removeChip(chip);
		}
		player.addScore(points);
		gameManager.addTooltip(
			player, String.format(
				"%s scores %d points",
				player.getNicknameToken(),
				points
			)
		);
		player.resetConnection();
	}

	public void removeCompleteConnections() {
		Player playerOne = gameManager.getPlayer(0);
		Player playerTwo = gameManager.getPlayer(1);
		int lengthOne = playerOne.getConnection().length;
		int lengthTwo = playerTwo.getConnection().length;
		int amountPlacedOne = playerOne.getConnection().amountPresentOnBoard;
		int amountPlacedTwo = playerTwo.getConnection().amountPresentOnBoard;
		if (lengthOne > lengthTwo) {
			//One wins
			removeCompleteConnection(playerOne);
		}
		else if (lengthTwo > lengthOne) {
			//Two wins
			removeCompleteConnection(playerTwo);
		}
		else {
			//Potential tie
			if (amountPlacedOne > amountPlacedTwo) {
				//One wins
				removeCompleteConnection(playerOne);
			}
			else if (amountPlacedTwo > amountPlacedOne) {
				//Two wins
				removeCompleteConnection(playerTwo);
			}
			else {
				//Tie
				removeCompleteConnection(playerOne);
				removeCompleteConnection(playerTwo);
			}
		}
	}

	public void performGameUpdate(Player player) {
		gameManager.addToGameSummary("Round:" + round );
		turn++;

		switch (currentFrameType) {
			case ACTIONS: {
				gameSummaryManager.addRound(round);
				performActionUpdate(player);
				break;
			}
			case DELETE_CHIPS: {
				removeCompleteConnections();
				nextFrameType = FrameType.ACTIONS;
				gameManager.setFrameDuration(Constants.DELETE_FRAME_DURATION);
				boolean drop = chipManager.shouldDrop(gravity);
				resetAllConnections();
				if (drop) {
					//If gaps are created by deleting:
					nextFrameType = FrameType.DROP_CHIPS;
				}
				else {
					//check if there are more connections to delete
					boolean winner = updateConnections();
					if (winner) {
						nextFrameType = FrameType.DELETE_CHIPS;
					}
					else {
						nextFrameType = FrameType.ACTIONS;
					}
				}
				break;
			}
			case DROP_CHIPS: {
				int biggest_hexes_moved = chipManager.dropChips(gravity);
				boolean winner = updateConnections();
				if (winner) {
					nextFrameType = FrameType.DELETE_CHIPS;
				}
				else {
					nextFrameType = FrameType.ACTIONS;
				}
				gameManager.setFrameDuration(biggest_hexes_moved * Constants.HEX_TRAVEL_DURATION);
				break;
			}
			case NEW_CHIP: {
				break;
			}
			case ROTATE_BOARD: {
				break;
			}
		}

		gameManager.addToGameSummary(gameSummaryManager.toString());
		gameSummaryManager.clear();

		if (gameOver()) {
			gameManager.endGame();
		} else {
			gameManager.setMaxTurns(turn + 1);
		}
	}

	public void resetAllConnections() {
		for (Player p : gameManager.getActivePlayers()) {
			p.resetConnection();
		}
	}

	public void performActionUpdate(Player player) {
		round++;

		Action action = player.getAction();
		if (action.isDrop()) {
			Chip chip = doDrop(player, action);
			gameManager.setFrameDuration(300); //TODO: make this less hardcoded
			//figure out if this chip has already "landed" (this doesnt work with MAP_RING_SIZE 1)
			if (chipManager.shouldDrop(gravity) == true) {
				nextFrameType = FrameType.DROP_CHIPS; //cell directly below is empty
			}
			else {
				boolean winner = updateConnections();
				nextFrameType = (winner == true) ?
						FrameType.DELETE_CHIPS :
						FrameType.ACTIONS;
			}
		} else if (action.isRotate()) {
			resetAllConnections(); //every old connection is potentially broken
			doRotate(player, action);
			int actualMovedCycles = (3 - (Math.abs(action.cycleAmount - 3)));
			System.err.println(actualMovedCycles);
			gameManager.setFrameDuration(Constants.ROTATION_CYCLE_TIME * actualMovedCycles);
			boolean drop = chipManager.shouldDrop(gravity);
			if (drop) {
				nextFrameType = FrameType.DROP_CHIPS;
			}
			else {
				//If nothing had to move, we can be sure that there are no new connections either
				nextFrameType = FrameType.ACTIONS;
			}
		}
	}

	public Map<HexCoord, Cell> getBoard() {
		return board.map;
	}

	private boolean gameOver() {
		if (gameManager.getActivePlayers().size() <= 1)
			return true;
		if (nextFrameType == FrameType.DROP_CHIPS || nextFrameType == FrameType.DELETE_CHIPS)
			return false;
		if (Config.MAX_ROUNDS != 0 && round >= Config.MAX_ROUNDS)
			return true;
		for (Player player : gameManager.getActivePlayers()) {
			if (player.getScore() >= Config.WIN_THRESHOLD)
				return true;
		}
		return false;
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

	public Map<Integer,Chip> getChips() {
		return this.chipManager.getChips();
	}
}
