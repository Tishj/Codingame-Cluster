package com.codingame.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

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

	public LinkedList<ConnData> sortAndGet(LinkedList<ConnData> unsorted) {
		//Priorities:
		//1. biggest length
		//2. most chips present on board
		unsorted.sort((ConnData a, ConnData b) -> {
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
		return unsorted;
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

		int highestChipIndex = chipManager.chips.lastKey() + 1;
		Integer[] chipCount = new Integer[highestChipIndex];
		Arrays.fill(chipCount, 0);

		while (getChipCount(connections, chipCount)) { //there are still unmerged sets
			System.err.println("Combine occurrences loop counter: " + counter++);
			LinkedList<ConnData> combined_connections = new LinkedList<>();

			//make sure every connection is only used once at the most
			boolean[] connectionUsed = new boolean[connections.size()];
			Arrays.fill(connectionUsed, false);

			for (int chipIndex = 0; chipIndex < highestChipIndex; chipIndex++) {
				int count = chipCount[chipIndex];
				chipCount[chipIndex] = 0;
				if (count < 2)
					continue;

				HashSet<Integer> combined = new HashSet<>();
				Iterator<ConnData> tmp = connections.iterator();
				ConnData connData = null;
	
				for (int conn = 0; count != 0 && tmp.hasNext(); conn++) {
					ConnData data = tmp.next();
					if (connectionUsed[conn])
						continue;

					HashSet<Integer> chips = data.chips;
					if (chips.contains(chipIndex)) {
						connectionUsed[conn] = true;
						count--;
						if (count == 0) {
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
			}
			// counter = counter;
			connections = combined_connections; //update the connections, for the next iteration
		}
		this.completedConnections = sortAndGet(connections);
		return this.completedConnections.get(0).chips;
	}

	public HashSet<Integer> combineConnections(LinkedList<ConnData> connections) {
		//create unordered_map of hashsets
		//key = chipIndex
		//value = combined connections this chip is in
		
		//for (key : keys)
		//	create a hashset of chipIndexes

		//we then loop over all the chips inside the set
		//look up the set that this chip is connected to
		//if not every chip is already a part of our collection, add it

		// int highestChipIndex = chipManager.chips.lastKey() + 1;
		TreeMap<Integer, ConnData>	individualClumps = new TreeMap<>();
		for (ConnData connection : connections) {
			for (int chipIndex : connection.chips) {
				if (!individualClumps.containsKey(chipIndex)) {
					individualClumps.put(chipIndex, connection);
				}
				else {
					individualClumps.get(chipIndex).chips.addAll(connection.chips);
				}
			}
		}

		for (Map.Entry<Integer, ConnData> entry : individualClumps.entrySet()) {
			int chipIndex = entry.getKey();
			ConnData connection = entry.getValue();
			HashSet<Integer> toAdd = new HashSet<>();
			for (Integer chip : connection.chips) {
				if (chip == chipIndex)
					continue;
				ConnData other = individualClumps.get(chip);
				if (!toAdd.addAll(other.chips)) {
					//??
				}
			}
			connection.chips.addAll(toAdd);
		}
		
		LinkedList<ConnData> sorted = sortAndGet(new LinkedList<ConnData>(individualClumps.values()));
		return sorted.get(0).chips;
	}

	public HashSet<Integer> remove() {
		if (this.completedConnections.size() == 0) {
			return new HashSet<>();
		}
		HashSet<Integer> removedChips = combineConnections(this.completedConnections);
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
