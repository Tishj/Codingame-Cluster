
package com.codingame.game;

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
import com.codingame.game.exception.CellNotFoundException;
import com.codingame.game.exception.CellNotValidException;
import com.codingame.game.exception.GameException;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Game {

	@Inject private MultiplayerGameManager<Player> gameManager;
	@Inject private GameSummaryManager gameSummaryManager;
	@Inject private ChipManager chipManager;

	public static int MAX_ROUNDS;

	Board		board;
	List<Chip>	placedChips;
	Gravity		gravity;
	List<Cell>	cells;
	Random		random;
	int			round = 0;
	int			turn = 0;

	public void init(long seed) {

		MAX_ROUNDS = Config.MAX_ROUNDS;

		random = new Random(seed);
		board = BoardGenerator.generate(random);
		cells = new ArrayList<>();
		chipManager.init(gameManager);

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
		lines.add(String.valueOf(round));
		//Player information, receiving player first
		Player other = gameManager.getPlayer(1 - player.getIndex());
		// lines.add(
		// 	String.format(
		// 		"%d %d",
		// 		player.getSun(),
		// 		player.getScore()
		// 	)
		// );
		// lines.add(
		// 	String.format(
		// 		"%d %d %d",
		// 		other.getSun(),
		// 		other.getScore(),
		// 		other.isWaiting() ? 1 : 0
		// 	)
		// );
		// lines.add(String.valueOf(trees.size()));
		// trees.forEach((index, tree) -> {
		// 	lines.add(
		// 		String.format(
		// 			"%d %d %d %d",
		// 			index,
		// 			tree.getSize(),
		// 			tree.getOwner() == player ? 1 : 0,
		// 			tree.isDormant() ? 1 : 0
		// 		)
		// 	);
		// });

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

	public void resetGameTurnData() {
		// dyingTrees.clear();
		// availableSun.clear();
		// sentSeeds.clear();
		// for (Player p : gameManager.getPlayers()) {
		// 	availableSun.add(p.getSun());
		// 	p.reset();
		// }
		// currentFrameType = nextFrameType;
	}

	private Chip doDrop(Player player, Action action) throws GameException {
		HexCoord startingLocation = board.getTopOfRow(gravity, action.targetId);
		if (startingLocation == null) {
			//really shouldnt happen
		}
		Cell cell = getBoard().get(startingLocation);
		if (cell == null || cell.getChip() != null) {
			//throw exception
			//position out of bounds, or already occupied by another chip
		}
		//create the chip
		Chip chip = chipManager.createChip(player, action.colorId, startingLocation);
		//add it to the chips list
		placedChips.add(chip);

		dropChip(chip);

		//move rest of selection back to remaining
		chipManager.emptySelectionForPlayer(player);
		return chip;
	}

	private void doRotate(Player player, Action action) throws GameException {
		gravity = gravity.rotate(action.cycleAmount);

		//Drop all chips
		for (Chip chip: placedChips) {
			dropChip(chip);
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
	public boolean dropChip(Chip chip) {
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
			if (neighbourChip != null && !dropChip(neighbourChip)) {
				break;
			}
			updateChipLocation(cell, neighbour, chip, coord);
			moved = true;
		}
		return moved;
	}

	public void performGameUpdate(Player player) {
		turn++;

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
			}
		} catch (GameException e) {
			gameSummaryManager.addError(player.getNicknameToken() + ": " + e.getMessage());
		}
		// gameManager.setFrameDuration(Constants.DURATION_ACTION_PHASE);
	}

	public Map<HexCoord, Cell> getBoard() {
		return board.map;
	}

	private boolean gameOver() {
		return gameManager.getActivePlayers().size() <= 1 || round >= MAX_ROUNDS;
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
}
