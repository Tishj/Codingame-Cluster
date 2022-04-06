package com.codingame.game;

public class Constants {
	//Frame durations
	public static int ROTATION_CYCLE_TIME = 500;
	public static int DELETE_FRAME_DURATION = 500;
	public static int HEX_TRAVEL_DURATION = 100;
	public static int DEFAULT_FRAME_DURATION = 500;
	public static int ACTION_DROP_FRAME_DURATION = 300;

	//Score
	public static int CHIP_VALUE = 20;
	public static int bonusPoints(int multiplier) {
		int bonus = 0;
		for (int i = 0; i < multiplier; i++) {
			bonus += bonus + 5;
		}
		return bonus;
	}
}
