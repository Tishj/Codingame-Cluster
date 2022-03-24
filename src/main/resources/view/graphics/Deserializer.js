const MAIN_SEPARATOR = '\n';
const PLAYER_AMOUNT = 2;

export function parseData(raw, globalData) {
    const input = raw.split(MAIN_SEPARATOR)
        .map(hardSplit);
    const globalInfos = input.shift().map(elem => +elem);
    const data = {
        round: globalInfos[0],
        players: []
    };
	let index;
    index = PLAYER_AMOUNT;
    for (let i = 1; i <= index; i++) {
        const player = input.shift().map(x => +x);
        data.players.push({
            score: player[0],
        });
    }
    return data;
}

//lines
// [
// 	[ "5", "4", "5" ], //every line
// 	[ "5", "4", "5" ],
// 	[ "5", "4", "5" ],
// 	[ "5", "4", "5" ],
// ]

export function parseGlobalData(raw) {
    const input = raw.split(MAIN_SEPARATOR).map(line => hardSplit(line).map(element => +element));
    const data = {
        totalRounds: input.shift()[0],
        cells: []
    };
    while (input.length) {
        const cell = input.shift();
        data.cells.push({
            q: cell[0],
            r: cell[1],
            index: cell[2]
        });
    }
    return data;
}
function hardSplit(raw) {
    if (raw === '') {
        return [];
    }
    return raw.split(' ');
}
