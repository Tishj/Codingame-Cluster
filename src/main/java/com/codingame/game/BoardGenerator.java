package com.codingame.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BoardGenerator {
	
	static private Map<HexCoord, Cell> board;
	static private int index = 0;
	
	public static void generateCell(HexCoord coord) {
		Cell cell = new Cell(index++, coord);
		board.put(coord, cell);
	}
	
	public static Board generate(Random random) {
		board = new HashMap<>();
		HexCoord coord = new HexCoord(0,0,0);
		generateCell(coord);
		coord = coord.neighbour(0);
		
		for (int distance = 1; distance < Config.MAP_RING_COUNT; distance++) {
			for (int orientation = 0; orientation < 6; orientation++) {
				for (int count = 0; count < distance; count++) {
					generateCell(coord);
					coord = coord.neighbour((orientation + 2) % 6);
				}
			}
			coord = coord.neighbour(0);
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
		