package com.codingame.view;

import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Module;
import com.google.inject.Inject;
import com.google.inject.Singleton;

//These functions are callbacks registered to certain events, basically
@Singleton
public class ViewModule implements Module {

	private GameManager<AbstractPlayer> gameManager;
	private GameDataProvider gameDataProvider;

	@Inject
	ViewModule(GameManager<AbstractPlayer> gameManager, GameDataProvider gameDataProvider) {
		this.gameManager = gameManager;
		this.gameDataProvider = gameDataProvider;
		gameManager.registerModule(this);
	}

	@Override
	public final void onGameInit() {
		sendGlobalData(); //send the global data (the cells)
		sendFrameData(); //send the volatile, frame specific data
	}

	private void sendFrameData() {
		FrameViewData data = gameDataProvider.getCurrentFrameData();
		gameManager.setViewData("graphics", Serializer.serialize(data));
	}

	private void sendGlobalData() {
		GlobalViewData data = gameDataProvider.getGlobalData();
		gameManager.setViewGlobalData("graphics", Serializer.serialize(data));

	}

	@Override
	public final void onAfterGameTurn() {
		sendFrameData();
	}

	@Override
	public final void onAfterOnEnd() {
	}

}
