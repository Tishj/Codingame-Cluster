package com.codingame.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.*;

@Singleton
public class Connection {
	@Inject ChipManager chipManager;
	HashSet<Integer>	lastAdded;

	public class ConnData {
		public int	color;
		public int playerId;
		public HashSet<Integer> chips;
		public ConnData(int color, int playerId, HashSet<Integer> chips) {
			this.color = color;
			this.chips = chips;
			this.playerId = playerId;
		}
	};
	LinkedList<ConnData> completedConnections;

	public void init() {
		this.completedConnections = new LinkedList<>();
		this.lastAdded = null;
	}

	public void reset() {
		this.completedConnections.clear();
	}

	public LinkedList<ConnData> sortAndGet() {
		//Priorities:
		//1. biggest length
		//2. most chips present on board
		this.completedConnections.sort((ConnData a, ConnData b) -> {
			if (a.chips.size() > b.chips.size())
				return 1; //a is bigger
			if (b.chips.size() > a.chips.size())
				return -1; //b is bigger;
			int a_amount = chipManager.placedChips[a.playerId][a.color];
			int b_amount = chipManager.placedChips[b.playerId][b.color];
			if (a_amount == b_amount) {
				return 0;
			}
			if (a_amount > b_amount) {
				return 1;
			}
			return -1;
		});
		return this.completedConnections;
	}

	public HashSet<Integer> remove() {
		if (this.completedConnections.size() == 0) {
			return new HashSet<>();
		}
		LinkedList<ConnData> connections = sortAndGet();
		HashSet<Integer> removedChips = new HashSet<>(chipManager.chips.size());

		ConnData connection = connections.get(0);

		removedChips.addAll(connection.chips);
		Iterator<ConnData> it = connections.iterator();
		it.next(); //Skip over the first connection
		while (it.hasNext()) {
			ConnData conn = it.next();
			//check if removed chips occur in the connection;
			if (!Collections.disjoint(conn.chips, removedChips)) {
				removedChips.addAll(conn.chips);
			}
		}
		invalidate(removedChips);
		return removedChips;
	}

	public void updateLastAdded(HashSet<Integer> chips) {
		this.lastAdded = chips;
	}

	public HashSet<Integer> getLastAdded() {
		return this.lastAdded;
	}

	public HashSet<Integer> invalidate(HashSet<Integer> chips) {
		HashSet<Integer> affectedChips = (HashSet<Integer>)chips.clone();
		for (int i = completedConnections.size() - 1; i >= 0; i--) {
			//Check if chips can be found in this connection;
			boolean invalid = !Collections.disjoint(completedConnections.get(i).chips, chips);
			if (invalid) {
				affectedChips.addAll(completedConnections.get(i).chips);
				completedConnections.remove(i);
			}
		}
		return affectedChips;
	}

	public void invalidateSingle(int chipIndex) {
		for (int i = completedConnections.size() - 1; i >= 0; i--) {
			//Check if chip can be found in this connection;
			boolean invalid = completedConnections.get(i).chips.contains(chipIndex);
			if (invalid) {
				completedConnections.remove(i);
			}
		}
	}

	//add the connection if it's big enough
	public boolean add(int color, Player player, HashSet<Integer> chips) {
		int placedChipsOfThisColor = chipManager.placedChips[player.getIndex()][color];
		//Not enough colors to even logicall form a connection
		if (placedChipsOfThisColor < Config.WIN_LENGTH)
			return false;
		//This connection is not big enough to form a completed connection
		if (chips.size() < Config.WIN_LENGTH)
			return false;
		//Check if this connection should replace an existing one (a "new" chip turns 4 -> 5)
		for (int i = 0; i < completedConnections.size(); i++) {
			if (color != completedConnections.get(i).color)
				continue;
			//chips is a superset of this connection
			if (chips.containsAll(completedConnections.get(i).chips)) {
				completedConnections.set(i,new ConnData(color, player.getIndex(), chips));
				return true;
			}
		}
		completedConnections.add(new ConnData(color, player.getIndex(), chips));
		return true;
	}
}
