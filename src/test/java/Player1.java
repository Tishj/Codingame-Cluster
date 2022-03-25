import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Player1 {

	private static class Chip {
		private int		index;
		private int		color;
		private int		cellIndex;
		private boolean isMine;

		public Chip(int index,
					int color,
					int cellIndex,
					boolean isMine) {
			this.index = index;
			this.color = color;
			this.cellIndex = cellIndex;
			this.isMine = isMine;
		}

		public void updatePosition(int cellIndex) {
			this.cellIndex = cellIndex;
		}

		public int getPosition() {
			return this.cellIndex;
		}
	}
	private static class Cell {
		private int index;
		private int[] neighbours;

		public Cell(int index,
					int[] neighbours) {
			this.index = index;
			this.neighbours = neighbours;
		}
	}

	private static class InsertPosition {
		private int[] cellIndex;
		public InsertPosition(int[] cellIndex) {
			this.cellIndex = cellIndex;
		}
		public int get(int direction) {
			return this.cellIndex[direction];
		}
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		Random random = new Random();
		long seed = random.nextLong();
		System.err.println("Player1 random seed is " + seed);
		//Start of Initial input
		int numberOfCells = in.nextInt();
		ArrayList<Cell>	board = new ArrayList<>(numberOfCells);
		for (int i = 0; i < numberOfCells; i++) {
			int index = in.nextInt();
			int[] neighbours = new int[6];
			for (int n = 0; n < 6; n++) {
				neighbours[n] = in.nextInt();
			}
			board.add(new Cell(index, neighbours));
		}

		int numberOfColumns = in.nextInt();
		ArrayList<InsertPosition>	columns = new ArrayList<>(numberOfColumns);
		ArrayList<Chip>				chips = new ArrayList<>(numberOfColumns);
		for (int i = 0; i < numberOfColumns; i++) {
			int[] directions = new int[6];
			for (int n = 0; n < 6; n++) {
				directions[n] = in.nextInt();
			}
			columns.add(new InsertPosition(directions));
		}
	
		int yourColors = in.nextInt();
		for (int i = 0; i < yourColors; i++) {
			int maxAmount = in.nextInt();
		}

		int opponentColors = in.nextInt();
		for (int i = 0; i < opponentColors; i++) {
			int maxAmount = in.nextInt();
		}
		//End of initial input

		while (true) {
			int gravity = in.nextInt();

			int numberOfInvalidColumns = in.nextInt();
			int[] invalid = new int[numberOfInvalidColumns];
			for (int i = 0; i < numberOfInvalidColumns; i++) {
				invalid[i] = in.nextInt();
			}

			int numberOfNewChips = in.nextInt();
			for (int i = 0; i < numberOfNewChips; i++) {
				int index = in.nextInt();
				int colorIndex = in.nextInt();
				int cellIndex = in.nextInt();
				int isMine = in.nextInt();
				chips.add(new Chip(index, colorIndex, cellIndex, isMine == 1));
			}

			//This is slightly fucked.. dont know whether a cell was already updated by another chip, so I cant just set the old cell to NULL
			//Maybe the player never needs to register in the Cell which Chip is on it ??
			int numberOfChangedChips = in.nextInt();
			for (int i = 0; i < numberOfChangedChips; i++) {
				int chipIndex = in.nextInt();
				int newCellIndex = in.nextInt();
				chips.get(chipIndex).updatePosition(newCellIndex);
			}

			int numberofColorsInHand = in.nextInt();
			int[] possibleColors = new int[numberofColorsInHand];
			for (int i = 0; i < numberofColorsInHand; i++) {
				possibleColors[i] = in.nextInt();
			}

			//Check which places are valid to place a Chip at
			List<Integer> validColumnIndices = IntStream.range(0,numberOfColumns).boxed().collect(Collectors.toList());
			for (int i = 0; i < invalid.length; i++) {
				validColumnIndices.remove(invalid[i]);
			}

			int column = random.nextInt(validColumnIndices.size());
			int color = possibleColors[random.nextInt(possibleColors.length)];
			System.out.printf("DROP %d %d%n", column, color);
		}
	}
}
