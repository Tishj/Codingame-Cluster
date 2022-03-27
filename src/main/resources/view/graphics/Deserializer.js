const MAIN_SEPARATOR = '\n';
const PLAYER_AMOUNT = 2;

export function parseData(raw, globalData) {
	const input = raw.split(MAIN_SEPARATOR)
		.map(hardSplit);
	const globalInfos = input.shift().map(elem => +elem);
	const data = {
		round: globalInfos[0],
		gravity: globalInfos[1],
		players: [],
		chips: []
	};
	let index;
	index = +input.shift()[0];
	for (let i = 0; i < index; i++) {
		//elem = "5" "10"
		const chip = input.shift().map(elem => +elem);
		data.chips.push({
			index: chip[0],
			color: chip[1],
			owner: chip[2],
			q: chip[3],
			r: chip[4]
		});
	}
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
	const globalInfos = input.shift();
	const data = {
		totalRounds: globalInfos[0],
		mapRingSize: globalInfos[1],
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
