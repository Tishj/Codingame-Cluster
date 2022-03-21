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
    @Inject private CommandManager commandManager;
    @Inject private Game game;
    @Inject private EndScreenModule endScreenModule;
    @Inject private ViewModule viewModule;
    @Inject private GameSummaryManager gameSummaryManager;

	private TicTacToeGrid masterGrid;
	private Action lastAction = null;
	private Random random;

	@Override
	public void init() {
		random = new Random(gameManager.getSeed());

		drawBackground();
		drawHud();
		drawGrids();

		gameManager.setFrameDuration(600);

		if (gameManager.getLeagueLevel() == 1) {
			gameManager.setMaxTurns(9);
		} else {
			gameManager.setMaxTurns(9 * 9);
		}
		
	}

	private void drawBackground() {
		graphicEntityModule.createSprite()
				.setImage("Background.jpg")
				.setAnchor(0);
		graphicEntityModule.createSprite()
				.setImage("logo.png")
				.setX(280)
				.setY(915)
				.setAnchor(0.5);
		graphicEntityModule.createSprite()
				.setImage("logoCG.png")
				.setX(1920 - 280)
				.setY(915)
				.setAnchor(0.5);
	}

	private void drawGrids() {
		int bigCellSize = 240;
		int bigOrigX = (int) Math.round(1920 / 2 - bigCellSize);
		int bigOrigY = (int) Math.round(1080 / 2 - bigCellSize);
		masterGrid = ticTacToeGridProvider.get();
		masterGrid.draw(bigOrigX, bigOrigY, bigCellSize, 5, 0xf9b700);

		if (gameManager.getLeagueLevel() == 2) {
			smallGrids = new TicTacToeGrid[3][3];
			
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					int cellSize = 60;
					int origX = bigOrigX + bigCellSize * i - cellSize;
					int origY = bigOrigY + bigCellSize * j - cellSize;
					smallGrids[j][i] = ticTacToeGridProvider.get();
					smallGrids[j][i].draw(origX, origY, cellSize, 3, 0xffffff);
				}
			}
		}
		graphicEntityModule
			.createSprite()
			.setImage("board_border.png")
			.setX(1920 / 2)
			.setY(1080 / 2)
			.setAnchor(0.5);
	}
	
	private void drawHud() {
		for (Player player : gameManager.getPlayers()) {
			int x = player.getIndex() == 0 ? 280 : 1920 - 280;
			int y = 220;

			graphicEntityModule
					.createRectangle()
					.setWidth(140)
					.setHeight(140)
					.setX(x - 70)
					.setY(y - 70)
					.setLineWidth(0)
					.setFillColor(player.getColorToken());

			graphicEntityModule
					.createRectangle()
					.setWidth(120)
					.setHeight(120)
					.setX(x - 60)
					.setY(y - 60)
					.setLineWidth(0)
					.setFillColor(0xffffff);

			Text text = graphicEntityModule.createText(player.getNicknameToken())
					.setX(x)
					.setY(y + 120)
					.setZIndex(20)
					.setFontSize(40)
					.setFillColor(0xffffff)
					.setAnchor(0.5);

			Sprite avatar = graphicEntityModule.createSprite()
					.setX(x)
					.setY(y)
					.setZIndex(20)
					.setImage(player.getAvatarToken())
					.setAnchor(0.5)
					.setBaseHeight(116)
					.setBaseWidth(116);

			player.hud = graphicEntityModule.createGroup(text, avatar);
		}
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

		// Read inputs
		try {
			final Action action = player.getAction();
			gameManager.addToGameSummary(String.format("Player %s played (%d %d)", action.player.getNicknameToken(), action.row, action.col));

			lastAction = action;

			final TicTacToeGrid grid;
			int winner = masterGrid.play(action);
			if (winner > 0) {
				setWinner(player);
			}
		} catch (NumberFormatException e) {
			player.deactivate("Wrong output!");
			player.setScore(-1);
			endGame();
		} catch (TimeoutException e) {
			gameManager.addToGameSummary(GameManager.formatErrorMessage(player.getNicknameToken() + " timeout!"));
			player.deactivate(player.getNicknameToken() + " timeout!");
			player.setScore(-1);
			endGame();
		} catch (InvalidAction e) {
			player.deactivate(e.getMessage());
			player.setScore(-1);
			endGame();
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
