package com.codingame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.codingame.view.ViewModule;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.game.action.Action;
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
	@Inject private Provider<TicTacToeGrid> ticTacToeGridProvider;
	@Inject private MultiplayerGameManager<Player> gameManager;
	@Inject private CommandParser commandParser;
	@Inject private Game game;
	@Inject private EndScreenModule endScreenModule;
	@Inject private ViewModule viewModule;
	@Inject private GameSummaryManager gameSummaryManager;

	private TicTacToeGrid masterGrid;
	private Action lastAction = null;
	private Random random;

	@Override
	public void init() {
		long seed = gameManager.getSeed();
		random = new Random(seed);

		try {
			Config.load(gameManager.getGameParameters());
			Config.export(gameManager.getGameParameters());
			gameManager.setFirstTurnMaxTime(1000);
			gameManager.setTurnMaxTime(100);

			gameManager.setFrameDuration(500);

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
	}

	private void setWinner(Player player) {
		gameManager.addToGameSummary(GameManager.formatSuccessMessage(player.getNicknameToken() + " won!"));
		player.setScore(10);
		endGame();
	}

	@Override
	public void gameTurn(int turn) {
		Player player = gameManager.getPlayer(turn % gameManager.getPlayerCount());
		
		sendInputs(player);
		player.execute();

		try {
			commandParser.parseCommands(player, player.getOutputs(), game);
			if (player.isActive()) {
				game.performGameUpdate(player);
			}
		}
		catch (TimeoutException e) {
			commandParser.deactivatePlayer(player, "Timeout!");
			gameSummaryManager.addPlayerTimeout(player);
			gameSummaryManager.addPlayerDisqualified(player);
		}
	}

	private void endGame() {
		gameManager.endGame();

		Player p0 = gameManager.getPlayers().get(0);
		Player p1 = gameManager.getPlayers().get(1);
		if (p0.getScore() > p1.getScore()) {
			p1.hud.setAlpha(0.3);
		}
		if (p0.getScore() < p1.getScore()) {
			p0.hud.setAlpha(0.3);
		}
	}
}
