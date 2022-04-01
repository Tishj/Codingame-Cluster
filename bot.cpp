#include <array>
#include <string>
#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
#include <time.h>
#include <map>
#include <unordered_set>
#include <unordered_map>
#include <queue>
#include <stack>
#include <memory>

typedef size_t idx_t;

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
		friend std::istream &operator>>( std::istream  &input, Side& side) {
			input >> side.side;
			return input;
		}
	private:
		Side(int side) : side(side) {}
	private:
};

class Cell;

class Chip {
	//public variables and constructor
	public:
		int32_t index;
		bool	isMine;
		int32_t	color;
		int32_t	cellIndex;
		Chip() {
			int32_t tmp;
			std::cin >> index >> color >> tmp >> cellIndex; std::cin.ignore();
			this->isMine = tmp == 1;
		}
		Chip(const Chip& other) :
			index(other.index),
			isMine(other.isMine),
			color(other.color),
			cellIndex(other.cellIndex) {}
	//public methods
	public:
		bool operator == (const Chip& other) const {
			return this->index == other.index;
		}
		int32_t getCellIndex() {
			return this->cellIndex;
		}
		// struct HashFunction {
		// 	size_t operator() (const Chip& chip) {
		// 		return (size_t)chip.index;
		// 	}
		// };
	//private methods
	private:
	//private variables
	private:
};

namespace std {
	template<>
	struct hash<Chip> {
		std::size_t operator() (const Chip& chip) const {
			return chip.index;
		}
	};
}

class Game {
	public:
		Side								side;
		std::vector<Cell>					board;
		std::vector<Chip> 					chips;
		std::vector<std::array<int32_t, 6>>	columns;
		std::vector<bool>					validColumns;
		std::array<std::vector<int32_t>, 2>	chipBag;
		std::vector<int32_t>				selectedChips;

		Game();
	public:
		void update();
		void	performRandomMove();
		std::vector<int32_t> getReachableEmptyNeighbours();
		std::unordered_map<int32_t, std::unordered_set<Chip>> getChipGroupings();
	private:
		void initBoard();
		void initColumns();
		void initBags();
		void updateSide();
		void updateValidColumns();
		void updateChips();
		void clearChipsFromCells();
		void updateSelection();
		void clearSelection();
		int32_t	getEmptyColumn();
		int32_t getSelectedColor();
	private:
};
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

class Cell {
	public:
		int32_t					index;
		std::array<int32_t, 6>	neighbours;
		int32_t					chip;
		Cell() : neighbours() {
			this->chip = -1;
			std::cin >> index >> neighbours[0] >> neighbours[1] >> neighbours[2] >> neighbours[3] >> neighbours[4] >> neighbours[5]; std::cin.ignore();
		}
	public:
		bool containsChip() const {
			return this->chip != -1;
		}
		bool isReachable() {
			Side direction = game->side.invert();
			int32_t cellIndex = this->index;
			do {
				if (cellIndex != this->index && game->board[cellIndex].containsChip()) {
					return false;
				}
				cellIndex = game->board[cellIndex].neighbours[direction];
			}
			while (cellIndex != -1);
			return true;
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
			stream << "ROTATE" << " " << obj.amount << std::endl;
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
			stream << "DROP" << " " << obj.column << " " << obj.color << std::endl;
			return stream;
		}
	private:
	private:
};

Game::Game() :
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

void Game::update() {
	updateSide();
	updateValidColumns();
	updateChips();
	updateSelection();
}

void	Game::performRandomMove() {
	if (rand() % 10 == 0) {
		std::cout << "ROTATE " << 1 + rand() % 5 << std::endl;
	}
	else {
		std::cout << "DROP " << getEmptyColumn() << " " << getSelectedColor() << std::endl;
	}
}

void Game::initBoard() {
	int32_t cellAmount; // amount of cells the board consists of
	std::cin >> cellAmount; std::cin.ignore();
	board.reserve(cellAmount);
	for (int32_t i = 0; i < cellAmount; i++) {
		board.emplace_back(Cell());
	}
}

void Game::initColumns() {
	int32_t numberOfColumns;
	std::cin >> numberOfColumns; std::cin.ignore();
	columns.reserve(numberOfColumns);
	validColumns.reserve(numberOfColumns);
	for (int32_t i = 0; i < numberOfColumns; i++) {
		std::array<int32_t, 6> cellIndices;
		std::cin >> cellIndices[0] >> cellIndices[1] >> cellIndices[2] >> cellIndices[3] >> cellIndices[4] >> cellIndices[5]; std::cin.ignore();
		columns.emplace_back(cellIndices);
		validColumns.push_back(false);
	}
}

void Game::initBags() {
	for (int32_t player = PLAYER_ME; player < PLAYER_SIZE; player++) {
		int32_t colorsAmount;
		std::cin >> colorsAmount; std::cin.ignore();
		if (player == PLAYER_ME) {
			this->selectedChips.reserve(colorsAmount);
			clearSelection();
		}

		chipBag[player].reserve(colorsAmount);
		for (int32_t i = 0; i < colorsAmount; i++) {
			int32_t maxAmount;
			std::cin >> maxAmount; std::cin.ignore();
			chipBag[player].push_back(maxAmount);
		}
	}
}

void Game::updateSide() {
	std::cin >> this->side; std::cin.ignore();
}

void Game::updateValidColumns() {
	int number_of_valid_columns; // the amount of columns that aren't currently filled
	std::cin >> number_of_valid_columns; std::cin.ignore();

	std::fill(validColumns.begin(), validColumns.end(), false);
	for (int32_t i = 0; i < number_of_valid_columns; i++) {
		int32_t column;
		std::cin >> column; std::cin.ignore();
		this->validColumns[column] = true;
	}
}

void Game::updateChips() {
	this->chips.clear();
	int number_of_chips; // the amount of chips currently on the board
	std::cin >> number_of_chips; std::cin.ignore();
	this->chips.reserve(number_of_chips);
	clearChipsFromCells();

	std::stack<int32_t>	cellsToEvaluate;

	for (int i = 0; i < number_of_chips; i++) {
		Chip chip;
		this->board[chip.cellIndex].chip = chip.index;
		this->chips.emplace_back(chip);
	}

	std::unordered_multiset<int32_t> reachableCells(cellsToEvaluate.size());
	while (cellsToEvaluate.size()) {
		const int32_t cellIndex = cellsToEvaluate.top();
		cellsToEvaluate.pop();

		Cell& cell = board[cellIndex];
		if (!cell.containsChip()) {
			bool reachable = cell.isReachable();
			if (reachable) {
				reachableCells.insert(cellIndex);
			}
		}
	}
}

std::vector<int32_t> Game::getReachableEmptyNeighbours() {
	std::vector<int32_t>	reachableCells(this->chips.size() * 3);

	//O(n*3)
	for (auto it = this->chips.begin(); it != this->chips.end(); it++) {
		Chip& chip = *it;
		if (chip.isMine) {
			const int32_t up = side.invert();
			const int32_t upLeft = side.invert().rotate(1);
			const int32_t upRight = side.invert().rotate(-1);

			const int32_t cellIndex = chip.cellIndex;
			Cell& cell = this->board[cellIndex];

			int32_t neighbours[3] = {};
			neighbours[0] = cell.neighbours[up];
			neighbours[1] = cell.neighbours[upRight];
			neighbours[2] = cell.neighbours[upLeft];
			for (idx_t i = 0 ; i < 3; i++) {
				const int32_t index = neighbours[i];
				if (index != -1 && !this->board[index].containsChip()) {
					reachableCells.push_back(index);
				}
			}
		}
	}
	return reachableCells;
}

std::unordered_map<int32_t, std::unordered_set<Chip>>	Game::getChipGroupings() {
	std::unordered_map<int32_t, std::unordered_set<Chip>> groupings(game->chips.size());
	//key: cellIndex;
	//val: connectedChips

	for (auto it = game->chips.begin(); it != game->chips.end(); it++) {
		Chip& chip = *it;
		if (!chip.isMine) {
			continue;
		}
		std::unordered_set<Chip>& group = groupings[chip.cellIndex];
		group.insert(chip);
		const Cell& cell = game->board[it->getCellIndex()];
		for (idx_t i = 0; i < 6; i++) {
			const int32_t neighbourCellIndex = cell.neighbours[i];
			if (neighbourCellIndex == -1) {
				continue;
			}
			const Cell& neighbourCell = game->board[neighbourCellIndex];
			if (!neighbourCell.containsChip()) {
				continue;
			}
			const Chip& neighbourChip = game->chips[neighbourCell.chip];
			if (!neighbourChip.isMine) {
				continue;
			}
			if (chip.color != neighbourChip.color) {
				continue;
			}
			std::unordered_set<Chip>& otherGroup = groupings[neighbourCellIndex];
			//TODO: maybe check if it's empty and then save the pair to evaluate later
			if (otherGroup.empty()) {
				otherGroup.insert(neighbourChip);
			}
			// group.merge(otherGroup);
			group.insert(otherGroup.begin(), otherGroup.end());
			otherGroup = group;
		}
	}
	return groupings;
}

void Game::clearChipsFromCells() {
	for (auto it = this->chips.begin(); it != this->chips.end(); it++) {
		this->board[it->cellIndex].chip = -1;
	}
}

void Game::updateSelection() {
	clearSelection();
	int number_of_colors_in_hand; // amount of chips drawn for you this round
	std::cin >> number_of_colors_in_hand; std::cin.ignore();
	for (int i = 0; i < number_of_colors_in_hand; i++) {
		int color_index;
		std::cin >> color_index; std::cin.ignore();
		this->selectedChips[color_index]++;
	}
}

void Game::clearSelection() {
	for (int32_t i = 0; i < this->chipBag[PLAYER_ME].size(); i++) {
		this->selectedChips.push_back(0);
	}
}

int32_t	Game::getEmptyColumn() {
	int32_t column = rand() % this->columns.size();
	while (!this->validColumns[column]) {
		column = (column + 1) % this->columns.size();
		dprintf(2, "getEmptyColumn loop\n");
	}
	return column;
}

int32_t Game::getSelectedColor() {
	int32_t color = rand() % this->chipBag[PLAYER_ME].size();
	while (this->selectedChips[color] == 0) {
		color = (color + 1) % this->chipBag[PLAYER_ME].size();
		dprintf(2, "getSelectedColor loop\n");
	}
	return color;
}

int main()
{
	game = std::make_unique<Game>();
	srand(time(NULL));

	while (1) {
		game->update();
		game->performRandomMove();
		//collect all the empty neighbours that are reachable
		std::vector<int32_t> cells = game->getReachableEmptyNeighbours();
		//make a unordered_map<cellIndex, list<connectedChips>>
		//then we can check the reachableNeighbours list, to see if we can connect two groups together
		// std::cout << "ROTATE 1" << std::endl;
	}
}
