package com.codingame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.codingame.view.ViewModule;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.game.action.Action;
import com.codingame.game.exception.GameException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Sprite;
import com.codingame.gameengine.module.entities.Text;
import com.codingame.gameengine.module.endscreen.EndScreenModule;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class Referee extends AbstractReferee {
	@Inject private MultiplayerGameManager<Player> gameManager;
	@Inject private CommandParser commandParser;
	@Inject private Game game;
	@Inject private EndScreenModule endScreenModule;
	@Inject private ViewModule viewModule;
	@Inject private GameSummaryManager gameSummaryManager;

	private Action lastAction = null;
	private Random random;

	@Override
	public void init() {
		long seed = gameManager.getSeed();
		random = new Random(seed);

		try {
			Config.load(gameManager);
			Config.export(gameManager.getGameParameters());
			System.out.println(Config.MAX_ROUNDS);
			gameManager.setFirstTurnMaxTime(1000);
			gameManager.setTurnMaxTime(100);

			gameManager.setFrameDuration(Constants.DEFAULT_FRAME_DURATION);
			game.init(seed);

			sendGlobalInfo();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Referee failed to initialize");
			abort();
		}
		
	}

	private void abort() {
		System.err.println("Unexpected game end");
		gameManager.endGame();
	}

	private void sendGlobalInfo() {
		for (Player player : gameManager.getActivePlayers()) {
			for (String line : game.getGlobalInfoFor(player)) {
				player.sendInputLine(line);
			}
		}
	}

	private void sendInputs(Player player) {
		//Send the updated gamestate
		for (String line : game.getCurrentFrameInfoFor(player)) {
			player.sendInputLine(line);
		}
	}

	public boolean handlePlayerCommand(Player player) {
		try {
			return commandParser.parseCommands(player, player.getOutputs(), game);
		}
		catch (TimeoutException e) {
			commandParser.deactivatePlayer(player, "Timeout!");
			gameSummaryManager.addPlayerTimeout(player);
			gameSummaryManager.addPlayerDisqualified(player);
			return false;
		}
	}

	@Override
	public void gameTurn(int turn) {
		//Ran out of chips;
		if (!game.setGameTurnData()) {
			endGame();
			return;
		}
		Player player = game.getCurrentPlayer();
		if (game.getCurrentFrameType() == FrameType.ACTIONS) {
			sendInputs(player);
			player.execute();
			if (!handlePlayerCommand(player)) {
				endGame();
				return;
			}
		}

		game.performGameUpdate(player);
	}

	private void endGame() {
		gameManager.endGame();
	}

	@Override
	public void onEnd() {
		endScreenModule.setTitleRankingsSprite("logo.png");

		int scores[] = gameManager.getPlayers().stream()
			.mapToInt(Player::getScore)
			.toArray();
		
		String displayedText[] = gameManager.getPlayers().stream()
			.map(Player::scoreToString)
			.toArray(String[]::new);
	 
		endScreenModule.setScores(scores, displayedText);
	}
}
