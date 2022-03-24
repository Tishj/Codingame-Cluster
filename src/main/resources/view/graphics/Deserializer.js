const MAIN_SEPARATOR = '\n';
export function parseData(raw, globalData) {
    const input = raw.split(MAIN_SEPARATOR)
        .map(hardSplit);
    const globalInfos = input.shift().map(x => +x);
    const data = {
        round: globalInfos[0],
        players: []
    };
	let index;
    index = 2;
    for (let i = 1; i <= index; i++) {
        const player = input.shift().map(x => +x);
        data.players.push({
            score: player[0],
        });
    }
    return data;
}

export function parseGlobalData(raw) {
    const input = raw.split(MAIN_SEPARATOR).map(x => hardSplit(x).map(y => +y));
    const data = {
        totalRounds: input.shift()[0],
        nutrients: input.shift(),
        cells: []
    };
    while (input.length) {
        const cell = input.shift();
        data.cells.push({
            q: cell[0],
            r: cell[1],
            richness: cell[2],
            index: cell[3]
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
