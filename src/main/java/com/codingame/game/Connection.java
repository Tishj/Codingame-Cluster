package com.codingame.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.lang.reflect.Array;
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
	
		// @Override public boolean equals(Object obj) {
		// 	if (obj == null)
		// 		return false;
		// 	if (this == obj)
		// 		return true;
		// 	if (getClass() != obj.getClass())
		// 		return false;
		// 	ConnData other = (ConnData) obj;
		// 	if (other.playerId != this.playerId)
		// 		return false;
		// 	if (other.color != this.color)
		// 		return false;
		// 	int other_size = other.chips.size();
		// 	int my_size = this.chips.size();
		// 	if (other_size < my_size)
		// 		return false;
		// 	if (other_size == my_size) {
		// 		return other.chips.containsAll(this.chips);
		// 	}
		// 	else {
		// 		//if we are a superset, dont 
		// 		return !this.chips.containsAll(other.chips);
		// 	}
		// }
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
				return -1; //a is bigger
			if (b.chips.size() > a.chips.size())
				return 1; //b is bigger;
			int a_amount = chipManager.placedChips[a.playerId][a.color];
			int b_amount = chipManager.placedChips[b.playerId][b.color];
			if (a_amount == b_amount) {
				return 0;
			}
			if (a_amount > b_amount) {
				return -1;
			}
			return 1;
		});
		return this.completedConnections;
	}

	//@return at least one of the chips 'count' is > 1
	private boolean getChipCount(LinkedList<ConnData> connections, Integer[] count) {
		Iterator<ConnData> it = connections.iterator();
		boolean connected = false;

		while (it.hasNext()) {
			ConnData current = it.next();
			HashSet<Integer> chips = current.chips;
			for (int chipIndex : chips) {
				count[chipIndex]++; //count how many times this chipIndex is seen in all the connections
				connected = connected || count[chipIndex] >= 2;
			}
		}
		return connected;
	}

	public HashSet<Integer>	getBiggest(LinkedList<ConnData> connections) {
		int counter = 0; //DEBUG

		Integer[] chipCount = new Integer[chipManager.chips.lastKey() + 1];
		Arrays.fill(chipCount, 0);

		while (getChipCount(connections, chipCount)) { //there are still unmerged sets
			System.err.println("Combine occurrences loop counter: " + counter++);
			LinkedList<ConnData> combined_connections = new LinkedList<>();

			//Loop over the entire occurrences array, creating combined sets where chips meet
			boolean[] connectionUsed = new boolean[connections.size()];
			Arrays.fill(connectionUsed, false);

			for (int chipIndex = 0; chipIndex < chipCount.length; chipIndex++) {
				if (chipCount[chipIndex] == 1) {
					chipCount[chipIndex]--;
				}
				if (chipCount[chipIndex] == 0)
					continue;
				HashSet<Integer> combined = new HashSet<>();
				Iterator<ConnData> tmp = connections.iterator();
				ConnData connData = null;
	
				for (int j = 0; chipCount[chipIndex] != 0 && tmp.hasNext(); j++) {
					ConnData data = tmp.next();
					if (connectionUsed[j])
						continue;

					HashSet<Integer> chips = data.chips;
					if (chips.contains(chipIndex)) {
						connectionUsed[j] = true;
						chipCount[chipIndex]--;
						if (chipCount[chipIndex] == 0) {
							connData = data;
						}
						combined.addAll(chips);
					}
				}
				// this is NULL if the set was already added to another connection
				if (connData != null) {
					ConnData newConnData = new ConnData(connData.color, connData.playerId, combined);
					combined_connections.add(newConnData);
				}
				chipCount[chipIndex] = 0;
			}
			// counter = counter;
			connections = combined_connections; //update the connections, for the next iteration
		}
		this.completedConnections = connections;
		this.completedConnections = sortAndGet();
		return this.completedConnections.get(0).chips;
	}

	public HashSet<Integer> remove() {
		if (this.completedConnections.size() == 0) {
			return new HashSet<>();
		}
		HashSet<Integer> removedChips = getBiggest(this.completedConnections);
		// HashSet<Integer> removedChips = new HashSet<>(chipManager.chips.size());

		// ConnData connection = connections.get(0);

		// removedChips.addAll(connection.chips);
		// Iterator<ConnData> it = connections.iterator();
		// it.next(); //Skip over the first connection
		// while (it.hasNext()) {
		// 	ConnData conn = it.next();
		// 	//check if removed chips occur in the connection;
		// 	if (!Collections.disjoint(conn.chips, removedChips)) {
		// 		removedChips.addAll(conn.chips);
		// 	}
		// }
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
		//Not enough colors to even logically form a connection
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
				completedConnections.get(i).chips = chips;
				return true;
			}
		}
		completedConnections.add(new ConnData(color, player.getIndex(), chips));
		return true;
	}
}
