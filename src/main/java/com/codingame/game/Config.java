package com.codingame.game;

import java.util.Properties;
import java.util.function.Function;

public class Config {

	public static int WIN_LENGTH = 4;
	public static int COLORS_PER_PLAYER = 2;
	public static int COLORS_PER_ROUND = 2;
	public static int MAX_ROUNDS = 24;
	public static int MAP_RING_COUNT = 4;
	public static int CELL_COUNT;


	public static void load(Properties params) {
		WIN_LENGTH = getFromParams(params, "WIN_LENGTH", WIN_LENGTH);
		COLORS_PER_PLAYER = getFromParams(params, "COLORS_PER_PLAYER", COLORS_PER_PLAYER);
		COLORS_PER_ROUND = getFromParams(params, "COLORS_PER_ROUND", COLORS_PER_ROUND);
		MAX_ROUNDS = getFromParams(params, "MAX_ROUNDS", MAX_ROUNDS);
		MAP_RING_COUNT = getFromParams(params, "MAP_RING_COUNT", MAP_RING_COUNT);
		CELL_COUNT = getCellCount();
		CHIP_MAX = getChipMax();
	}

	private int getChipMax() {
		int max = (int)(CELL_COUNT / 4);
		max -= max % (COLORS_PER_PLAYER * 2);
		return max;
	}

	private int getCellCount() {
		int count = 0;
		for (int i = 0; i < MAP_RING_COUNT; i++) {
			count += 6 * i;
		}
		return count;
	}

	public static void export(Properties params) {
	}

	private static int getFromParams(Properties params, String name, int defaultValue) {
		return getFromParams(params, name, defaultValue, Integer::valueOf);
	}

	private static <T> T getFromParams(Properties params, String name, T defaultValue, Function<String, T> convert) {
		String inputValue = params.getProperty(name);
		if (inputValue != null) {
			try {
				return convert.apply(inputValue);
			} catch (NumberFormatException e) {
				// Do naught
			}
		}
		return defaultValue;
	}
}
