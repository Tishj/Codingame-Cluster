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
}
