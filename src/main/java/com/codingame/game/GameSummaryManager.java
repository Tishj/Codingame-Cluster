package com.codingame.game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.codingame.gameengine.core.GameManager;
import com.google.inject.Singleton;

@Singleton
public class GameSummaryManager {
	private List<String> lines;

	public GameSummaryManager() {
		this.lines = new ArrayList<>();
	}

	public String getSummary() {
		return toString();
	}

	public void clear() {
		this.lines.clear();
	}

	public void addPlayerBadCommand(Player player, InvalidInputException invalidInputException) {
		lines.add(
			GameManager.formatErrorMessage(
				String.format(
					"%s provided invalid input. Expected '%s'\nGot '%s'",
					player.getNicknameToken(),
					invalidInputException.getExpected(),
					invalidInputException.getGot()
				)
			)
		);
	}

	public void addPlayerTimeout(Player player) {
		lines.add(
			GameManager.formatErrorMessage(
				String.format(
					"%s has not provided an action in time.",
					player.getNicknameToken()
				)
			)
		);
	}

	public void addPlayerDisqualified(Player player) {
		lines.add(
			String.format(
				"%s was disqualified.",
				player.getNicknameToken()
			)
		);
	}

	// public void addCutTree(Player player, Cell cell, int score) {
	// 	lines.add(
	// 		String.format(
	// 			"%s is ending their tree life on cell %d, scoring %d points",
	// 			player.getNicknameToken(),
	// 			cell.getIndex(),
	// 			score
	// 		)
	// 	);
	// }

	public void addRound(int round) {
		lines.add(
			String.format(
				"Round %d/%d",
				round,
				Config.MAX_ROUNDS - 1
			)
		);
	}

	public void addError(String error) {
		lines.add(error);
	}

	@Override
	public String toString() {
		return lines.stream().collect(Collectors.joining("\n"));
	}

	// public void addRoundTransition(int round) {
	// 	lines.add(
	// 		String.format(
	// 			"Round %d ends",
	// 			round
	// 		)
	// 	);
	// 	if (round + 1 < Config.MAX_ROUNDS) {
	// 		lines.add(
	// 			String.format(
	// 				"The sun is now pointing towards direction %d",
	// 				(round + 1) % 6
	// 			)
	// 		);
	// 		lines.add(
	// 			String.format(
	// 				"Round %d starts",
	// 				round + 1
	// 			)
	// 		);
	// 	}
	// }
}
