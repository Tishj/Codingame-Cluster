
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

	public static boolean ENABLE_SEED;
	public static boolean ENABLE_GROW;
	public static boolean ENABLE_SHADOW;
	public static boolean ENABLE_HOLES;
	public static int MAX_ROUNDS;
	public static int STARTING_TREE_COUNT;
	public static int STARTING_TREE_SIZE;
	public static int STARTING_TREE_DISTANCE;
	public static boolean STARTING_TREES_ON_EDGES;

	Board board;
	List<Chip> placedChips;
	Gravity gravity;
	Map<Integer, Integer> shadows;
	List<Cell> cells;
	Random random;
	int round = 0;
	int turn = 0;

	public void init(long seed) {

		MAX_ROUNDS = Config.MAX_ROUNDS;

		random = new Random(seed);
		board = BoardGenerator.generate(random);
		cells = new ArrayList<>();

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

	private void doDrop(Player player, Action action) throws GameException {
		HexCoord coord = getCoordByIndex(action.getTargetId());
		Cell cell = board.map.get(coord);
		// Chip chip = ChipGenerator

	}

	// private boolean aChipIsOn(Cell cell) {
	// 	return trees.containsKey(cell.getIndex());
	// }

	public void performGameUpdate() {
		turn++;

		//Perform logic here

		gameManager.addToGameSummary(gameSummaryManager.toString());
		gameSummaryManager.clear();

		if (gameOver()) {
			gameManager.endGame();
		} else {
			gameManager.setMaxTurns(turn + 1);
		}
	}

	public void performActionUpdate() {
		gameManager.getPlayers()
			.stream()
			.filter(p -> !p.isWaiting())
			.forEach(player -> {
				try {
					Action action = player.getAction();
					if (action.isGrow()) {
						doGrow(player, action);
					} else if (action.isSeed()) {
						doSeed(player, action);
					} else if (action.isComplete()) {
						doComplete(player, action);
					} else {
						player.setWaiting(true);
						gameSummaryManager.addWait(player);
					}
				} catch (GameException e) {
					gameSummaryManager.addError(player.getNicknameToken() + ": " + e.getMessage());
					player.setWaiting(true);
				}
			});

		// gameManager.setFrameDuration(Constants.DURATION_ACTION_PHASE);

	}

	private Tree placeTree(Player player, int index, int size) {
		Tree tree = new Tree();
		tree.setSize(size);
		tree.setOwner(player);
		trees.put(index, tree);
		return tree;
	}

	public Map<HexCoord, Cell> getBoard() {
		return board.map;
	}

	public Map<Integer, Integer> getShadows() {

		return shadows;
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
