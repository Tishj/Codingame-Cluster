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
#include <assert.h>

typedef size_t idx_t;

#define NO_NEIGHBOUR -1
#define NO_CHIP -1
#define NOT_REACHABLE -1
#define WIN_LENGTH 3

static const char* sideToString[] = {
	"SOUTH",
	"SOUTH WEST",
	"NORTH WEST",
	"NORTH",
	"NORTH EAST",
	"NORTH WEST",
};

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
		Side(int side) : side(side) {}
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
	private:
};

class Cell;

class Chip {
	//public variables and constructor
	public:
		std::array<Chip*, 6>	connections;
		int32_t 				index;
		bool					isMine;
		int32_t					color;
		int32_t					cellIndex;
		Chip() : connections() {
			int32_t tmp;
			std::cin >> index >> color >> tmp >> cellIndex; std::cin.ignore();
			this->isMine = tmp == 1;
		}
		Chip(const Chip& other) :
			connections(other.connections),
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
		std::vector<std::unique_ptr<Chip>> 	chips;
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
		int32_t findColumn(int32_t cellIndex);
	private:
		void initBoard();
		void initColumns();
		void initBags();
		void updateSide();
		void updateValidColumns();
		void updateChips();
		void updateChipConnections();
		void clearChipsFromCells();
		void updateSelection();
		void clearSelection();
		int32_t	getEmptyColumn();
		int32_t getSelectedColor();
		bool containsSameChip(const Chip& reference, int32_t cellIndex);
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
			this->chip = NO_CHIP;
			std::cin >> index >> neighbours[0] >> neighbours[1] >> neighbours[2] >> neighbours[3] >> neighbours[4] >> neighbours[5]; std::cin.ignore();
		}
	public:
		bool containsChip() const {
			return this->chip != NO_CHIP;
		}
		int32_t isReachable() {
			Side direction = game->side.invert();
			int32_t cellIndex = this->index;
			int32_t last = cellIndex;
			do {
				if (cellIndex != this->index && game->board[cellIndex].containsChip()) {
					return NOT_REACHABLE;
				}
				last = cellIndex;
				cellIndex = game->board[cellIndex].neighbours[direction];
			}
			while (cellIndex != NO_NEIGHBOUR);
			return last;
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
	updateChipConnections();
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
		columns.push_back(std::array<int32_t, 6>());
		std::array<int32_t, 6>& cellIndices = columns[i];
		std::cin >> cellIndices[0] >> cellIndices[1] >> cellIndices[2] >> cellIndices[3] >> cellIndices[4] >> cellIndices[5]; std::cin.ignore();
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
	clearChipsFromCells();
	this->chips.clear();
	int number_of_chips; // the amount of chips currently on the board
	std::cin >> number_of_chips; std::cin.ignore();
	this->chips.reserve(number_of_chips);
	for (idx_t i = 0; i < this->board.size(); i++) {
		this->chips.emplace_back(nullptr);
	}

	for (int i = 0; i < number_of_chips; i++) {
		std::unique_ptr<Chip> chip = std::make_unique<Chip>(Chip());
		this->board[chip->cellIndex].chip = chip->index;
		this->chips[chip->index].swap(chip);
	}
}

void Game::updateChipConnections() {
	for (auto it = this->chips.begin(); it != this->chips.end(); it++) {
		if (it->get() == nullptr)
			continue;
		Chip& chip = *(it->get());
		auto& connections = chip.connections;
		for (idx_t i = 0; i < 6; i++) {
			const int32_t neighbourCellIndex = this->board[chip.cellIndex].neighbours[i];
			chip.connections[i] = NULL;
			if (!containsSameChip(chip, neighbourCellIndex)) {
				continue;
			}
			chip.connections[i] = this->chips[this->board[neighbourCellIndex].chip].get();
		}
	}
}

std::vector<int32_t> Game::getReachableEmptyNeighbours() {
	std::vector<int32_t>	reachableCells;

	//O(n*3)
	for (auto it = this->chips.begin(); it != this->chips.end(); it++) {
		if (it->get() == NULL)
			continue;
		Chip& chip = *(it->get());
		if (!chip.isMine)
			continue;
		Cell& cell = game->board[chip.cellIndex];
		for (idx_t dir = 0 ; dir < 6; dir++) {
			//there cant be a vacant cell directly under the one that is being occupied by this chip
			if (dir == game->side) {
				continue;
			}
			const int32_t index = cell.neighbours[dir];
			if (index == NO_NEIGHBOUR) {
				continue;
			}
			Cell& other = this->board[index];
			if (other.containsChip()) {
				continue;
			}
			//if there's no chip underneath this, a chip cant end up here
			if (other.neighbours[side] != NO_NEIGHBOUR && !this->board[other.neighbours[side]].containsChip()) {
				continue ;
			}
			if (other.isReachable() == NOT_REACHABLE) {
				continue;
			}
			reachableCells.push_back(index);
		}
	}
	return reachableCells;
}

bool Game::containsSameChip(const Chip& reference, int32_t cellIndex) {
	if (cellIndex == NO_NEIGHBOUR)
		return false;
	const Cell& cell = game->board[cellIndex];
	if (!cell.containsChip())
		return false;
	const Chip& other = *(game->chips[cell.chip]);
	if (other.color != reference.color)
		return false;
	if (other.isMine != reference.isMine)
		return false;
	return true;
}

//kinda useless? this saves clusters, not necesarrily groups
std::unordered_map<int32_t, std::unordered_set<Chip>>	Game::getChipGroupings() {
	std::unordered_map<int32_t, std::unordered_set<Chip>> groupings(game->chips.size());
	//key: cellIndex;
	//val: connectedChips

	for (auto it = game->chips.begin(); it != game->chips.end(); it++) {
		if (it->get() == nullptr)
			continue;
		Chip& chip = *(it->get());
		if (!chip.isMine) {
			continue;
		}
		std::unordered_set<Chip>& group = groupings[chip.cellIndex];
		group.insert(chip);
		const Cell& cell = game->board[it->get()->getCellIndex()];
		for (idx_t i = 0; i < 6; i++) {
			const int32_t neighbourCellIndex = cell.neighbours[i];
			if (!containsSameChip(chip, neighbourCellIndex))
				continue;
			Chip& neighbourChip = *(game->chips[game->board[neighbourCellIndex].chip]);
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
		if (it->get() == NULL)
			continue;
		this->board[it->get()->cellIndex].chip = NO_CHIP;
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

Chip* getChipAtCellPosition(int32_t cellIndex, bool mine, int32_t color) {
	if (cellIndex == NO_NEIGHBOUR)
		return NULL;
	Cell& neighbour = game->board[cellIndex];
	if (neighbour.chip == NO_CHIP) {
		return NULL;
	}
	Chip& chip = *(game->chips[neighbour.chip]);
	if (chip.isMine != mine) {
		return NULL;
	}
	if (chip.color != color) {
		return NULL;
	}
	return &chip;
}

void	getConnectionSize(Chip* chip, idx_t direction, std::vector<int32_t>& connection) {
	while (chip) {
		connection.push_back(chip->index);
		chip = chip->connections[direction];
	}
}

int32_t Game::findColumn(int32_t cellIndex) {
	// assert(cellIndex != -1);
	if (cellIndex == NO_NEIGHBOUR) {
		return -1; //throw exception
	}
	int32_t columnCellIndex = this->board[cellIndex].isReachable();
	for (idx_t i = 0; i < this->columns.size(); i++) {
		if (this->columns[i][side] == columnCellIndex) {
			return i;
		}
	}
	return -1; //throw exception
}

bool createAndSendDrop(int32_t cellIndex, int32_t color) {
	std::cout << "DROP" << " " << game->findColumn(cellIndex) << " " << color << std::endl;
	std::cerr << "MADE AN INFORMED MOVE" << std::endl;
	return true;
}

bool makeInformedDecision(std::vector<int32_t>& cells) {
	//check every reachableCell
	for (auto it = cells.begin(); it != cells.end(); it++) {
		//check half of all directions
		Cell& cell = game->board[*it];
		for (idx_t color = 0; color < game->selectedChips.size(); color++) {
			if (game->selectedChips[color] == 0) {
				continue;
			}
			for (idx_t dir = 0; dir < 3; dir++) {
				std::vector<int32_t> connection;
				connection.reserve(WIN_LENGTH);
				Side side(dir);
				Chip* chip = getChipAtCellPosition(cell.neighbours[side], true, color);
				if (chip) {
					getConnectionSize(chip, side, connection);
				}
				side = side.invert();
				chip = getChipAtCellPosition(cell.neighbours[side], true, color);
				if (chip) {
					getConnectionSize(chip, side, connection);
				}
				if (connection.size() + 1 >= WIN_LENGTH) {
					dprintf(2, "FOUND A CONNECTION IN DIRECTION '%s'-'%s' OF SIZE %ld DROPPING AT %d\n", sideToString[dir], sideToString[side], connection.size() + 1, *it);
					for (idx_t i = 0; i < connection.size(); i++) {
						dprintf(2, "\tCell: %d\n", connection[i]);
					}
					return createAndSendDrop(*it, color);
				}
			}
		}
	}
	return false;
}

std::ostream& operator << (std::ostream& stream, Side& side) {
	stream << sideToString[(int32_t)side] << "\n";
	return stream;
}

std::ostream& operator << (std::ostream& stream, Chip& obj) {
	stream << "-- Chip --" << "\n";
	stream << "cellIndex: " << obj.cellIndex << "\n";
	stream << "color: " << obj.color << "\n";
	stream << "isMine: " << std::boolalpha << obj.isMine << "\n";
	stream << "index: " << obj.index << std::endl;
	return stream;
}

std::ostream& operator << (std::ostream& stream, Cell& obj) {
	stream << "-- Cell --" << "\n";
	stream << "index: " << obj.index << "\n";
	stream << "chipIndex: " << obj.chip << "\n";
	for (idx_t i = 0; i < 6; i++) {
		stream << "side[" << sideToString[i] << "] = " << obj.neighbours[i] << std::endl;
	}
	return stream;
}

std::ostream& operator << (std::ostream& stream, Game& obj) {
	stream << "-- Game --" << "\n";
	stream << "cellAmount: " << obj.board.size() << "\n";
	for (idx_t i = 0; i < obj.board.size(); i++) {
		stream << obj.board[i] << std::endl;
	}
	stream << "chipsAmount: " << obj.chips.size() << "\n";
	for (idx_t i = 0; i < obj.chips.size(); i++) {
		if (obj.chips[i].get() == NULL)
			continue;
		stream << *(obj.chips[i]) << std::endl;
	}
	stream << "columnAmount: " << obj.columns.size() << "\n";
	for (idx_t i = 0; i < obj.columns.size(); i++) {
		stream << "-- Column --" << "\n";
		stream << "index: " << i << "\n";
		for (idx_t j = 0; j < 6; j++) {
			stream << "side[" << sideToString[j] << "] = cellIndex: " << obj.columns[i][j] << "\n";
		}
	}
	stream << "side: " << sideToString[(int32_t)obj.side] << "\n";
	return stream;
}

void	debugReachableCells(std::vector<int32_t>& cells) {
	dprintf(2, "Amount of reachable cells: %ld\n", cells.size());
	for (idx_t i = 0; i < cells.size(); i++) {
		dprintf(2, "cell[%ld] = %d\n", i, cells[i]);
	}
}

int main()
{
	game = std::make_unique<Game>();
	srand(time(NULL));

	while (1) {
		game->update();
		// std::cerr << *game;
		//collect all the empty neighbours that are reachable
		std::vector<int32_t> cells = game->getReachableEmptyNeighbours();
		// debugReachableCells(cells);
		if (!makeInformedDecision(cells))
			game->performRandomMove();
		//make a unordered_map<cellIndex, list<connectedChips>>
		//then we can check the reachableNeighbours list, to see if we can connect two groups together
		// std::cout << "ROTATE 1" << std::endl;
	}
}
