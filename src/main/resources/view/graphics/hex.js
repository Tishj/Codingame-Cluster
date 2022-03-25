import { TILE_HEIGHT } from './assetConstants.js';
const TILE_SEPERATION = 0.1;
export const HEXAGON_HEIGHT = TILE_HEIGHT + TILE_SEPERATION;
export const HEXAGON_RADIUS = HEXAGON_HEIGHT / 2;
export const HEXAGON_WIDTH = HEXAGON_RADIUS * Math.sqrt(3);
export const HEXAGON_Y_SEP = HEXAGON_RADIUS * 3 / 2;
export function hexToScreen(q, r) {
	const separator = Math.sqrt(3);
	const s = -q - r;
	const y = -(HEXAGON_RADIUS * (separator * s + separator / 2 * q));
	const x = HEXAGON_RADIUS * 3 / 2 * q;
	return { x, y };
}
