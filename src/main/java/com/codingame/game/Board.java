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

	public HexCoord getTopOfRow(Gravity gravity, int row) {
		HexCoord coord = new HexCoord(0,0,0);
		for (int i = 0; i < Config.MAP_RING_COUNT; i++) {
			coord = coord.neighbour(gravity);
		}

		//steps to get to the desired cell
		Gravity stepDirection = gravity.rotate(-1);
		if (row > Config.MAP_RING_COUNT) {
			row -= Config.MAP_RING_COUNT;
			stepDirection = gravity.rotate(1);
		}

		//travel to the cell
		for (int i = 0; i < row; i++) {
			coord = coord.neighbour(stepDirection);
		}
		return coord;
	}
}
