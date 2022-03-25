package com.codingame.game;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Board {

	public final Map<HexCoord, Cell> map;
	public final List<HexCoord> coords;

	public Board(Map<HexCoord, Cell> map) {
		this.map = map;
		coords = map.entrySet()
			.stream()
			.sorted(
				(a, b) -> a.getValue().getIndex() - b.getValue().getIndex()
			)
			.map(Entry::getKey)
			.collect(Collectors.toList());
	}

	public HexCoord getTopOfColumn(Gravity gravity, int column) {
		gravity = gravity.rotate(3);
		HexCoord coord = new HexCoord(0,0,0);
		for (int i = 1; i < Config.MAP_RING_COUNT; i++) {
			coord = coord.neighbour(gravity);
		}
	
		int middleColumn = Config.MAP_RING_COUNT - 1;

		//steps to get to the desired cell
		Gravity stepDirection = gravity.rotate(-2);
		if (column > middleColumn) {
			column -= middleColumn;
			stepDirection = gravity.rotate(2);
		}
		else if (column < middleColumn) {
			column = middleColumn - column;
		}
		else {
			return coord;
		}

		//travel to the cell
		for (int i = 0; i < column; i++) {
			coord = coord.neighbour(stepDirection);
		}
		return coord;
	}
}
