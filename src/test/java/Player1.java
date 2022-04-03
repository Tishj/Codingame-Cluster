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
					boolean isMine) {
			this.index = index;
			this.color = color;
			this.cellIndex = -1;
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
		private int chipIndex;

		public Cell(int index,
					int[] neighbours) {
			this.index = index;
			this.chipIndex = -1;
			this.neighbours = neighbours;
		}
		public void setChipIndex(int chipIndex) {
			this.chipIndex = chipIndex;
		}
		public int getChipIndex() {
			return this.chipIndex;
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

	private static int getFirstVacantCellIndex(ArrayList<InsertPosition> positions, ArrayList<Cell> board, int gravity) {
		for (InsertPosition position : positions) {
			if (board.get(position.cellIndex[gravity]).chipIndex == -1) {
				return position.cellIndex[gravity];
			}
		}
		return -1;
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		long seed = new Random().nextLong();
		// long seed = 3049155287956461818L;
		Random random = new Random(seed);
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
		ArrayList<Chip>				chips = new ArrayList<>(numberOfCells);
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

			int numberOfValidColumns = in.nextInt();
			List<Integer>	validColumns = new ArrayList<>(numberOfColumns);
			for (int i = 0; i < numberOfValidColumns; i++) {
				validColumns.add(in.nextInt());
			}

			for (Cell cell : board) {
				cell.setChipIndex(-1);
			}
			
			int numberOfChips = in.nextInt();
			chips.clear();
			for (int i = 0; i < numberOfChips; i++) {
				int index = in.nextInt();
				int colorIndex = in.nextInt();
				int isMine = in.nextInt();
				int cellIndex = in.nextInt();
				Chip chip = new Chip(index, colorIndex, isMine == 1);
				chips.add(chip);
				chip.updatePosition(cellIndex);
				board.get(cellIndex).setChipIndex(index);
			}

			int numberOfPelletsInHand = in.nextInt();
			int[] possibleColors = new int[numberOfPelletsInHand];
			for (int i = 0; i < numberOfPelletsInHand; i++) {
				possibleColors[i] = in.nextInt();
			}

			boolean rotate = random.nextInt(10) == 0;
			if (rotate) {
				System.out.printf("ROTATE %d%n", 1 + random.nextInt(5));
			}
			else {
				int cellIndex = validColumns.get(random.nextInt(validColumns.size()));
				int color = possibleColors[random.nextInt(possibleColors.length)];
				System.out.printf("DROP %d %d%n", cellIndex, color);
			}
		}
	}
}
