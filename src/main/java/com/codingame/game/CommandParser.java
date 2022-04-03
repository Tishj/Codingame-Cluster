package com.codingame.game;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codingame.game.action.DropAction;
import com.codingame.game.action.RotateAction;
import com.codingame.game.exception.CellAlreadyOccupiedException;
import com.codingame.game.exception.ChipNotSelectedException;
import com.codingame.game.exception.ColorOutOfRangeException;
import com.codingame.game.exception.ColumnOutOfRangeException;
import com.codingame.game.exception.CycleOutOfRangeException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CommandParser {
	@Inject private GameSummaryManager gameSummaryManager;
	@Inject private ChipManager chipManager;

	static final Pattern PLAYER_ROTATE_PATTERN = Pattern.compile(
		"^ROTATE (?<cycleAmount>\\d+)"
	);

	static final Pattern PLAYER_DROP_PATTERN = Pattern.compile(
		"^DROP (?<targetId>\\d+) (?<colorId>\\d+)"
	);

	private int findColumn(Cell[] cells, int cellIndex) {
		for (int i = 0; i < cells.length; i++) {
			if (cells[i].index == cellIndex) {
				return i;
			}
		}
		return -1;
	}

	public void parseCommands(Player player, List<String> lines, Game game) {
		String command = lines.get(0);

		try {
			Matcher match;

			match = PLAYER_ROTATE_PATTERN.matcher(command);
			if (match.matches()) {
				int cycleAmount = Integer.parseInt(match.group("cycleAmount"));
				if (cycleAmount < 1 || cycleAmount > 5) {
					throw new CycleOutOfRangeException(cycleAmount);
				}
				player.setAction(new RotateAction(cycleAmount));
				return;
			}

			match = PLAYER_DROP_PATTERN.matcher(command);
			if (match.matches()) {
				int targetId = Integer.parseInt(match.group("targetId"));
				int column = findColumn(game.insertionPositions[game.gravity.getIndex()], targetId);
				if (column == -1) {
					throw new ColumnOutOfRangeException(targetId);
				}
				int colorId = Integer.parseInt(match.group("colorId"));
				if (colorId < 0 || colorId >= Config.COLORS_PER_PLAYER) {
					throw new ColorOutOfRangeException(colorId);
				}
				if (!chipManager.colorIsSelected(player, colorId)) {
					throw new ChipNotSelectedException(colorId);
				}
				if (game.insertionPositions[game.getGravity().getIndex()][column].getChip() != null) {
					throw new CellAlreadyOccupiedException(targetId);
				}
				player.setAction(new DropAction(column, colorId));
				return;
			}
			throw new InvalidInputException(Game.getExpected(), command);

		} catch (InvalidInputException e) {
			deactivatePlayer(player, e.getMessage());
			gameSummaryManager.addPlayerBadCommand(player, e);
			gameSummaryManager.addPlayerDisqualified(player);
		} catch (Exception e) {
			InvalidInputException invalidInputException = new InvalidInputException(Game.getExpected(), e.toString());
			deactivatePlayer(player, invalidInputException.getMessage());
			gameSummaryManager.addPlayerBadCommand(player, invalidInputException);
			gameSummaryManager.addPlayerDisqualified(player);
		}
	}

	public void deactivatePlayer(Player player, String message) {
		player.deactivate(escapeHTMLEntities(message));
		player.setScore(-1);
	}

	private String escapeHTMLEntities(String message) {
		return message
			.replace("&lt;", "<")
			.replace("&gt;", ">");
	}
}
