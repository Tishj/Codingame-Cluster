package com.codingame.game;

import java.util.Properties;
import java.util.function.Function;

import com.codingame.gameengine.core.MultiplayerGameManager;

public class Config {
	public static final int WOOD_II = 1;
	public static final int WOOD_I = 2;
	public static final int BRONZE = 3;
	public static final int SILVER = 4;

	public static int WIN_LENGTH = 3;
	public static int COLORS_PER_PLAYER = 1;
	public static int COLORS_PER_ROUND = 1;
	public static int MAP_RING_COUNT = 6;
	public static int CELL_COUNT;
	public static int CHIP_MAX;
	public static int COLUMN_COUNT;
	public static int MAX_ROUNDS;
	public static int WIN_THRESHOLD = 2000;
	public static boolean ROTATE_ENABLED = true;

	public static void load(MultiplayerGameManager<Player> gameManager) {
		Properties params = gameManager.getGameParameters();

		WIN_LENGTH = getFromParams(params, "WIN_LENGTH", WIN_LENGTH);
		COLORS_PER_PLAYER = getFromParams(params, "COLORS_PER_PLAYER", COLORS_PER_PLAYER);
		COLORS_PER_ROUND = getFromParams(params, "COLORS_PER_ROUND", COLORS_PER_ROUND);
		MAX_ROUNDS = getFromParams(params, "MAX_ROUNDS", MAX_ROUNDS);
		MAP_RING_COUNT = getFromParams(params, "MAP_RING_COUNT", MAP_RING_COUNT);
		int league = gameManager.getLeagueLevel();
		switch (league) {
			case WOOD_II: {
				WIN_LENGTH = 3;
				MAP_RING_COUNT = 3;
				COLORS_PER_PLAYER = 1;
				WIN_THRESHOLD = 1;
				ROTATE_ENABLED = false;
				break;
			}
			case WOOD_I: {
				WIN_LENGTH = 3;
				MAP_RING_COUNT = 3;
				COLORS_PER_PLAYER = 2;
				WIN_THRESHOLD = 1;
				ROTATE_ENABLED = false;
				break;
			}
			case BRONZE: {
				WIN_LENGTH = 4;
				MAP_RING_COUNT = 4;
				COLORS_PER_PLAYER = 2;
				WIN_THRESHOLD = 100;
				break;
			}
			case SILVER: {
				WIN_LENGTH = 4;
				MAP_RING_COUNT = 5;
				COLORS_PER_PLAYER = 2;
				WIN_THRESHOLD = 200;
			}
			default: {
				break;
			}
		}
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
