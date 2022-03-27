package com.codingame.game;

import java.util.Properties;
import java.util.function.Function;

public class Config {

	public static int WIN_LENGTH = 3;
	public static int COLORS_PER_PLAYER = 1;
	public static int COLORS_PER_ROUND = 2;
	public static int MAP_RING_COUNT = 4;
	public static int CELL_COUNT;
	public static int CHIP_MAX;
	public static int COLUMN_COUNT;
	public static int MAX_ROUNDS;
	public static int WIN_THRESHOLD = 200;

	public static void load(Properties params) {
		WIN_LENGTH = getFromParams(params, "WIN_LENGTH", WIN_LENGTH);
		COLORS_PER_PLAYER = getFromParams(params, "COLORS_PER_PLAYER", COLORS_PER_PLAYER);
		COLORS_PER_ROUND = getFromParams(params, "COLORS_PER_ROUND", COLORS_PER_ROUND);
		MAX_ROUNDS = getFromParams(params, "MAX_ROUNDS", MAX_ROUNDS);
		MAP_RING_COUNT = getFromParams(params, "MAP_RING_COUNT", MAP_RING_COUNT);
		CELL_COUNT = getCellCount();
		CHIP_MAX = getChipMax();
		COLUMN_COUNT = getColumnCount();
		MAX_ROUNDS = CHIP_MAX * COLORS_PER_PLAYER * 2;
	}

	private static int getColumnCount() {
		return (Config.MAP_RING_COUNT * 2) -1;
	}

	private static int getChipMax() {
		int max = (int)(CELL_COUNT / (2 * COLORS_PER_PLAYER));
		max -= max % (COLORS_PER_PLAYER * 2);
		return max;
	}

	private static int getCellCount() {
		return (MAP_RING_COUNT * MAP_RING_COUNT - MAP_RING_COUNT) * 3 + 1;
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
