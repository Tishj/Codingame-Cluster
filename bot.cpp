#include <array>
#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
#include <time.h>
#include <map>
#include <unordered_set>
#include <queue>
#include <stack>
#include <memory>

using namespace std;

typedef size_t idx_t;

class Game;

std::unique_ptr<Game> game;

enum Player {
	PLAYER_ME,
	PLAYER_OPP,
	PLAYER_SIZE
};

// enum BoardSide {
// 	SOUTH,
// 	SOUTH_WEST,
// 	NORTH_WEST,
// 	NORTH,
// 	NORTH_EAST,
// 	SOUTH_EAST
// };

class Side {
	public:
		enum e_side {
			SOUTH,
			SOUTH_WEST,
			NORTH_WEST,
			NORTH,
			NORTH_EAST,
			SOUTH_EAST
		};
		int32_t	side;
		Side() : side(SOUTH) {}
	public:
		Side invert() {
			return (*this) + 3;
		}
		Side rotate(int n) {
			if (n > 0) {
				return *this + n;
			}
			return *this - (n *= -1);
		}
		Side operator + (int n) {
			int side = this->side;
			side += n;
			return Side(side % 6);
		}
		Side operator += (int n) {
			this->side += n;
			this->side %= 6;
			return (*this);
		}
		Side operator - (int n) {
			int side = this->side;
			side = 6 - (abs(n) % 6);
			return Side(side);
		}
		Side operator -= (int n) {
			this->side = 6 - (abs(n) % 6);
			return (*this);
		}
		operator int32_t() {
			return this->side;
		}
		bool operator == (int32_t side) {
			return this->side == side;
		}
		bool operator == (const Side& other) {
			return this->side == other.side;
		}
		Side& operator = (int32_t side) {
			this->side = side;
			return *this;
		}
		friend istream &operator>>( istream  &input, Side& side) {
			input >> side.side;
			return input;
		}
	private:
		Side(int side) : side(side) {}
	private:
};

class Chip {
	//public variables and constructor
	public:
		int32_t index;
		bool	isMine;
		int32_t	color;
		int32_t	cellIndex;
		Chip() {
			int32_t tmp;
			cin >> index >> color >> tmp >> cellIndex; cin.ignore();
			this->isMine = tmp == 1;
		}
	//public methods
	public:
		int32_t getCellIndex() {
			return this->cellIndex;
		}
	//private methods
	private:
	//private variables
	private:
};

class Cell {
	public:
		int32_t					index;
		std::array<int32_t, 6>	neighbours;
		int32_t					chip;
		Cell() : neighbours() {
			this->chip = -1;
			cin >> index >> neighbours[0] >> neighbours[1] >> neighbours[2] >> neighbours[3] >> neighbours[4] >> neighbours[5]; cin.ignore();
		}
	public:
		bool containsChip() {
			return this->chip != -1;
		}
		bool isReachable() {
			// Side direction = game->side.invert();
			return false;
		}
	private:
	private:
};

class GameState {
	public:
	public:
	private:
	private:
};


class Rotate {
	public:
		int32_t	amount;
		Rotate(int32_t amount) : amount(amount) {}
	public:
		friend std::ostream& operator << (std::ostream& stream, const Rotate& obj) {
			stream << "ROTATE" << " " << obj.amount << endl;
			return stream;
		}
	private:
	private:
};

class Drop {
	public:
		int32_t	column;
		int32_t	color;
		Drop(int32_t column, int32_t color) : column(column), color(color) {}
	public:
		friend std::ostream& operator << (std::ostream& stream, const Drop& obj) {
			stream << "DROP" << " " << obj.column << " " << obj.color << endl;
			return stream;
		}
	private:
	private:
};

class Game {
	public:
		Side								side;
		std::vector<Cell>					board;
		std::vector<Chip> 					chips;
		std::vector<std::array<int32_t, 6>>	columns;
		std::vector<bool>					validColumns;
		std::array<std::vector<int32_t>, 2>	chipBag;
		std::vector<int32_t>				selectedChips;

		Game() :
			board(),
			chips(),
			columns(),
			validColumns(),
			chipBag(),
			selectedChips()
		{
			initBoard();
			initColumns();
			initBags();
			this->side = Side::SOUTH;
		}
	public:
		void update() {
			updateSide();
			updateValidColumns();
			updateChips();
			updateSelection();
		}

		void	performRandomMove() {
			if (rand() % 10 == 0) {
				cout << "ROTATE " << 1 + rand() % 5 << endl;
			}
			else {
				cout << "DROP " << getEmptyColumn() << " " << getSelectedColor() << endl;
			}
		}
	private:
		void initBoard() {
			int32_t cellAmount; // amount of cells the board consists of
			cin >> cellAmount; cin.ignore();
			board.reserve(cellAmount);
			for (int32_t i = 0; i < cellAmount; i++) {
				board.emplace_back(Cell());
			}
		}

		void initColumns() {
			int32_t numberOfColumns;
			cin >> numberOfColumns; cin.ignore();
			columns.reserve(numberOfColumns);
			validColumns.reserve(numberOfColumns);
			for (int32_t i = 0; i < numberOfColumns; i++) {
				std::array<int32_t, 6> cellIndices;
				cin >> cellIndices[0] >> cellIndices[1] >> cellIndices[2] >> cellIndices[3] >> cellIndices[4] >> cellIndices[5]; cin.ignore();
				columns.emplace_back(cellIndices);
				validColumns.push_back(false);
			}
		}

		void initBags() {
			for (int32_t player = PLAYER_ME; player < PLAYER_SIZE; player++) {
				int32_t colorsAmount;
				cin >> colorsAmount; cin.ignore();
				if (player == PLAYER_ME) {
					this->selectedChips.reserve(colorsAmount);
					clearSelection();
				}

				chipBag[player].reserve(colorsAmount);
				for (int32_t i = 0; i < colorsAmount; i++) {
					int32_t maxAmount;
					cin >> maxAmount; cin.ignore();
					chipBag[player].push_back(maxAmount);
				}
			}
		}

		void updateSide() {
			cin >> this->side; cin.ignore();
		}

		void updateValidColumns() {
			int number_of_valid_columns; // the amount of columns that aren't currently filled
			cin >> number_of_valid_columns; cin.ignore();

			std::fill(validColumns.begin(), validColumns.end(), false);
			for (int32_t i = 0; i < number_of_valid_columns; i++) {
				int32_t column;
				cin >> column; cin.ignore();
				this->validColumns[column] = true;
			}
		}

		void updateChips() {
			this->chips.clear();
			int number_of_chips; // the amount of chips currently on the board
			cin >> number_of_chips; cin.ignore();
			this->chips.reserve(number_of_chips);
			clearChipsFromCells();

			std::stack<int32_t>	cellsToEvaluate;

			for (int i = 0; i < number_of_chips; i++) {
				Chip chip;
				this->board[chip.cellIndex].chip = chip.index;
				this->chips.emplace_back(chip);

				//Add the neighbours
				if (this->chips[i].isMine) {
					const int32_t up = side.invert();
					const int32_t upLeft = side.invert().rotate(1);
					const int32_t upRight = side.invert().rotate(-1);

					const int32_t cellIndex = this->chips[i].cellIndex;
					Cell& cell = this->board[cellIndex];

					int32_t neighbours[3] = {};
					neighbours[0] = cell.neighbours[up];
					neighbours[1] = cell.neighbours[upRight];
					neighbours[2] = cell.neighbours[upLeft];
					for (idx_t i = 0 ; i < 3; i++) {
						const int32_t index = neighbours[i];
						if (index != -1 && !this->board[index].containsChip()) {
							cellsToEvaluate.push(index);
						}
					}
				}
			}

			std::unordered_multiset<int32_t> reachableCells(cellsToEvaluate.size());
			while (cellsToEvaluate.size()) {
				const int32_t cellIndex = cellsToEvaluate.top();
				cellsToEvaluate.pop();

				Cell& cell = board[cellIndex];
				if (!cell.containsChip()) {
					bool reachable = cell.isReachable();
				}
			}
		}

		void clearChipsFromCells() {
			for (auto it = this->chips.begin(); it != this->chips.end(); it++) {
				this->board[it->cellIndex].chip = -1;
			}
		}

		void updateSelection() {
			clearSelection();
			int number_of_colors_in_hand; // amount of chips drawn for you this round
			cin >> number_of_colors_in_hand; cin.ignore();
			for (int i = 0; i < number_of_colors_in_hand; i++) {
				int color_index;
				cin >> color_index; cin.ignore();
				this->selectedChips[color_index]++;
			}
		}

		void clearSelection() {
			for (int32_t i = 0; i < this->chipBag[PLAYER_ME].size(); i++) {
				this->selectedChips.push_back(0);
			}
		}

		int32_t	getEmptyColumn() {
			int32_t column = rand() % this->columns.size();
			while (!this->validColumns[column]) {
				column = (column + 1) % this->columns.size();
				dprintf(2, "getEmptyColumn loop\n");
			}
			return column;
		}

		int32_t getSelectedColor() {
			int32_t color = rand() % this->chipBag[PLAYER_ME].size();
			while (this->selectedChips[color] == 0) {
				color = (color + 1) % this->chipBag[PLAYER_ME].size();
				dprintf(2, "getSelectedColor loop\n");
			}
			return color;
		}
	private:
};

int main()
{
	game = std::make_unique<Game>();
	srand(time(NULL));

	while (1) {
		game->update();
		game->performRandomMove();
		// cout << "ROTATE 1" << endl;
	}
}
