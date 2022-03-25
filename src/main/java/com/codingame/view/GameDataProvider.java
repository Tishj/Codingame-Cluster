package com.codingame.view;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codingame.game.Cell;
import com.codingame.game.Game;
import com.codingame.game.Player;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.codingame.game.HexCoord;

//Get and convert the data from Game to the format expected by the ViewModule

@Singleton
public class GameDataProvider {
	@Inject private Game game;
	@Inject private MultiplayerGameManager<Player> gameManager;

	public FrameViewData getCurrentFrameData() {
		FrameViewData data = new FrameViewData();

		data.chips = game.getChips()
			.stream()
			.map(chip -> {
				ChipData chipData = new ChipData();
				chipData.color = chip.getColorId();
				chipData.owner = chip.getOwner().getIndex();
				chipData.index = chip.getIndex();
				chipData.q = chip.getCoord().getQ();
				chipData.r = chip.getCoord().getR();
				return chipData;
			})
			.collect(Collectors.toList());

		data.gravity = game.getGravity().getIndex();
		data.players = gameManager.getPlayers()
			.stream()
			.map(player -> {

				PlayerData playerData = new PlayerData();
				playerData.score = player.getScore();
				return playerData;
			})
			.collect(Collectors.toList());

		data.round = game.getRound();

		return data;
	}

	public GlobalViewData getGlobalData() {
		GlobalViewData data = new GlobalViewData();
		data.cells = game.getBoard()
			.entrySet()
			.stream()
			.map(entry -> {

				HexCoord coord = entry.getKey();
				Cell cell = entry.getValue();

				CellData cellData = new CellData();
				cellData.q = coord.getQ();
				cellData.r = coord.getR();
				cellData.index = cell.getIndex();
				return cellData;
			})
			.collect(Collectors.toList());

		data.totalRounds = Game.MAX_ROUNDS;
		return data;
	}
}
