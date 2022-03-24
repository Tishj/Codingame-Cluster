import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Player3 {

	private static class Chip {
		int index;
		int color;
		boolean isMine;
		int cellIndex;
	}
	private static class Cell {
		int index;
		private int[] neighbours = new int[6];
		public Cell(Scanner in) {
			
		}
	}

	Map<Integer, Cell>	board;

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		Random random = new Random(0);

		//Start of Initial input
		int numberOfCells = in.nextInt();
		for (int i = 0; i < numberOfCells; i++) {
			int index = in.nextInt();
			for (int n = 0; n < 6; n++) {
				int neighbourIndex = in.nextInt();
			}
		}

		int numberOfColumns = in.nextInt();
		for (int i = 0; i < numberOfColumns; i++) {
			int columnIndex = in.nextInt();
			for (int direction = 0; direction < 6; direction++) {
				int cellIndex = in.nextInt();
			}
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
			for (int i = 0; i < numberOfInvalidColumns; i++) {
				int column = in.nextInt();
			}

			int numberOfNewChips = in.nextInt();
			for (int i = 0; i < numberOfNewChips; i++) {
				int index = in.nextInt();
				int cellIndex = in.nextInt();
				int colorIndex = in.nextInt();
				int isMine = in.nextInt();
			}
			
			int numberOfChangedChips = in.nextInt();
			for (int i = 0; i < numberOfChangedChips; i++) {
				int chipIndex = in.nextInt();
				int cellIndex = in.nextInt();
			}

			int numberOfPelletsInHand = in.nextInt();
			for (int i = 0; i < numberOfPelletsInHand; i++) {
				int colorIndex = in.nextInt();
			}
			System.out.printf("DROP %d %d%n", 0, 0);
		}
	}
}
