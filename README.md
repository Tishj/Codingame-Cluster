VALID MOVES:  
`DROP column color`  
`ROTATE cycles` (1-5 are valid amounts for `cycles`)

If an invalid move is made, or a bot times out, the opposing player wins and the game is over.

<img src="https://github.com/Tishj/Codingame-Cluster/blob/main/config/resources/directions.png?raw=true" alt="drawing" width="300"/>

## Mappings
`gravity`  
0 - South  
1 - SouthWest  
2 - NorthWest  
3 - North  
4 - NorthEast  
5 - SouthEast  
## Input data

### Initialization Input

First line: `numberOfCells`  
Next `numberOfCells` lines: 7 space-separated integers:  
- `index` for the cell's index.  
- 6 `neigh` variables, one for each direction, containing the index of a neighboring cell or -1 if is there is no neighbor.  

Next line: An integer `numberOfColumns`
for `numberOfColumns` lines: 6 space-separated integers
- 6 cellIndex variables, one for each direction, containing the index of the cell that is at the top of that column, for that direction.

Next line: An integer `yourColors`  
Next `yourColors` lines: 2 space-separated integers:
- `index` for the color's index.  
- `maxAmount` for the max amount of chips of that color.  

Next line: An integer `opponentColors`  
Next `opponentColors` lines: 2 space-separated integers:
- `index` for the color's index.  
- `maxAmount` for the max amount of chips of that color.  

### Input for One Game Turn
First line: An integer `gravity` (between 0 and 5): the direction of gravity.  

Next line: `numberOfValidColumns`  
next `numberOfValidColumns` lines: 1 integer 
- `column`: the column to use for a DROP command

Next line: `numberOfChips`  
next `numberOfChips` lines: 4 space-separated integers:  
- `index`: index of the new chip.  
- `colorIndex`: index of the chip's color.  
- `isMine`: 1 if you are the owner of this chip, 0 otherwise.  
- `cellIndex`: index of the cell the chip is on.  

Next line: `numberOfChipsInHand`
next `numberOfChipsInHand` lines: 1 integer:
- `colorIndex`: index of the chip's color.

## GAME END CONDITIONS

### Loss
If you run out of chips  
If you timeout(bot only)  
If you make an invalid move  

### Win
Make a match bigger or equal to 4, and bigger than your opponent  
If both players have an equal size match, the amount of chips they have on the board of their winning color will serve as tie-breaker  

### Tie
Both players have a match of equal size, with equal number of tiles of their winning color on the board  

Invalid moves:  
Move is not among the valid moves, or does not follow the correct format for the move  
DROP command was done for a column that is already full
ROTATE command specified an invalid amount of cycles.
Sending a command before reading all the input  
