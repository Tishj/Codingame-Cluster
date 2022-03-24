package com.codingame.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BoardGenerator {

	static private Map<HexCoord, Cell> board;
	static private int index = 0;

	public static void generateCell(HexCoord coord) {
		Cell cell = new Cell(index++);
		board.put(coord, cell);
	}

	public static Board generate(Random random) {
		board = new HashMap<>();
		for (int q = -Config.MAP_RING_COUNT + 1; q < Config.MAP_RING_COUNT; q++) {
			for (int r = -Config.MAP_RING_COUNT + 1; r < Config.MAP_RING_COUNT; r++) {
				for (int s = -Config.MAP_RING_COUNT + 1; s < Config.MAP_RING_COUNT; s++) {
					if (q + r + s == 0) {
						generateCell(new HexCoord(q, r, s));
					}
				}
			}
		}

		createHoles(random);

		return new Board(board);
	}

	private static void createHoles(Random random) {
		// List<HexCoord> coordList = new ArrayList<>(board.keySet());
		// int coordListSize = coordList.size();
		// int wantedEmptyCells = Game.ENABLE_HOLES ? random.nextInt(Config.MAX_EMPTY_CELLS + 1) : 0;
		// int actualEmptyCells = 0;

		// while (actualEmptyCells < wantedEmptyCells - 1) {
		// 	int randIndex = random.nextInt(coordListSize);
		// 	HexCoord randCoord = coordList.get(randIndex);
		// 	board.get(randCoord).setRichness(Constants.RICHNESS_NULL);
		// 	actualEmptyCells++;
		// 	if (!randCoord.equals(randCoord.getOpposite())) {
		// 		board.get(randCoord.getOpposite()).setRichness(Constants.RICHNESS_NULL);
		// 		actualEmptyCells++;
		// 	}
		// }
	}
}
