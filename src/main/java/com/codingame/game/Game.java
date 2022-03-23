
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

	//TODO: implement this
	private Chip doDrop(Player player, Action action) throws GameException {
		//Get coord of the top of the row to drop the chip;

		//create the chip

		//add it to the chips list

		//drop the chip

		//move rest of selection back to remaining
		return chipManager.createChip(player, 0, new HexCoord(0,0,0));
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

	public void dropChip(Chip chip) {
		Map<HexCoord, Cell> board = getBoard();
		//Move it downwards
		while (true) {
			HexCoord coord = chip.getCoord();
			Cell cell = board.get(coord);
			coord = coord.neighbour(gravity);
			Cell neighbour = board.get(coord);
			//If we've reached the bottom of the board, we're done
			if (neighbour == null) {
				break;
			}
			Chip neighbourChip = neighbour.getChip();
			//Cell is vacant
			if (neighbourChip == null) {
				cell.setChip(null);
				neighbour.setChip(chip);
				cell = neighbour;
			}
			//Cell is already occupied
			else {
				dropChip(neighbourChip);
				neighbourChip = neighbour.getChip();
				//Chip hasn't moved
				if (neighbourChip != null) {
					break;
				}
			}
		}
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
				Chip chip = doDrop(player, action);
			} else if (action.isRotate()) {
				doRotate(player, action);
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
