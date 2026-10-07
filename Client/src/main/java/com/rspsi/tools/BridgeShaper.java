package com.rspsi.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Works out the heights of a bridge: a flat deck on the selected tiles and a gentle ramp around it that runs down to
 * the surrounding terrain. Pure calculation, so it can be tested without a loaded map.
 * <p>
 * Tiles are identified by {@code x << 16 | y}. Heights follow the cache: negative is higher.
 */
public class BridgeShaper {

	public static class Params {
		/** Number of tiles around the deck over which the terrain slopes down. 0 gives a hard edge. */
		public int rampWidth = 2;
		/** Fixed deck height (negative = higher), or {@link #AUTO} to use the highest point under the deck. */
		public int deckHeight = AUTO;
	}

	public static final int AUTO = Integer.MAX_VALUE;

	/** The new height of every tile that has to change; tiles outside the map are never included. */
	public static class Result {
		public final Map<Integer, Integer> deck = new HashMap<>();
		public final Map<Integer, Integer> ramp = new HashMap<>();
	}

	public static int key(int x, int y) {
		return x << 16 | y;
	}

	/**
	 * The tiles of a straight-lined deck drawn along {@code line} (tile centres): every tile whose centre is within half
	 * the width of the line. Tiles outside the map are left out.
	 */
	public static Set<Integer> deckTiles(java.util.List<double[]> line, double deckWidth, int mapWidth, int mapLength) {
		Set<Integer> tiles = new HashSet<>();
		if (line.isEmpty())
			return tiles;
		double reach = Math.max(deckWidth, 1) / 2.0;
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		for (double[] p : line) {
			minX = Math.min(minX, p[0]);
			minY = Math.min(minY, p[1]);
			maxX = Math.max(maxX, p[0]);
			maxY = Math.max(maxY, p[1]);
		}
		for (int x = Math.max(0, (int) Math.floor(minX - reach)); x <= Math.min(mapWidth - 1, (int) Math.ceil(maxX + reach)); x++) {
			for (int y = Math.max(0, (int) Math.floor(minY - reach)); y <= Math.min(mapLength - 1, (int) Math.ceil(maxY + reach)); y++) {
				if (distanceToLine(line, x + 0.5, y + 0.5) <= reach + 1e-9)
					tiles.add(key(x, y));
			}
		}
		return tiles;
	}

	/**
	 * Distance to the line, with flat ends: the deck stops at the first and last point instead of reaching past them.
	 * Bends in between are rounded so the deck has no gaps there.
	 */
	private static double distanceToLine(java.util.List<double[]> line, double px, double py) {
		if (line.size() == 1)
			return Math.hypot(px - line.get(0)[0], py - line.get(0)[1]);
		double best = Double.MAX_VALUE;
		int last = line.size() - 2;
		for (int i = 0; i <= last; i++) {
			double ax = line.get(i)[0], ay = line.get(i)[1], bx = line.get(i + 1)[0], by = line.get(i + 1)[1];
			double dx = bx - ax, dy = by - ay;
			double len2 = dx * dx + dy * dy;
			double raw = len2 == 0 ? 0 : ((px - ax) * dx + (py - ay) * dy) / len2;
			if ((i == 0 && raw < 0) || (i == last && raw > 1))
				continue;
			double t = Math.max(0, Math.min(1, raw));
			best = Math.min(best, Math.hypot(px - (ax + t * dx), py - (ay + t * dy)));
		}
		return best;
	}

	/**
	 * @param deck         the tiles that become the bridge deck
	 * @param lowerHeights heights of the level the bridge crosses, used to find the deck height
	 * @param upperHeights heights of the level the bridge is built on, the ramp slopes down to these
	 */
	public static Result shape(Set<Integer> deck, int[][] lowerHeights, int[][] upperHeights, Params params) {
		Result result = new Result();
		if (deck.isEmpty())
			return result;

		int width = upperHeights.length;
		int length = upperHeights[0].length;

		int deckHeight = params.deckHeight;
		if (deckHeight == AUTO) {
			deckHeight = 0;
			for (int key : deck)
				deckHeight = Math.min(deckHeight, lowerHeights[key >> 16][key & 0xFFFF]);
		}

		for (int key : deck)
			result.deck.put(key, deckHeight);

		int reach = Math.max(params.rampWidth, 0);
		Set<Integer> seen = new HashSet<>(deck);
		for (int key : deck) {
			int dx = key >> 16, dy = key & 0xFFFF;
			for (int x = Math.max(dx - reach, 0); x <= Math.min(dx + reach, width - 1); x++) {
				for (int y = Math.max(dy - reach, 0); y <= Math.min(dy + reach, length - 1); y++) {
					if (seen.add(key(x, y)))
						result.ramp.put(key(x, y), 0);
				}
			}
		}

		// slope by the distance to the nearest deck tile; with a ramp of 0 nothing is added above
		for (Map.Entry<Integer, Integer> entry : result.ramp.entrySet()) {
			int x = entry.getKey() >> 16, y = entry.getKey() & 0xFFFF;
			double nearest = Double.MAX_VALUE;
			for (int key : deck) {
				double ddx = x - (key >> 16), ddy = y - (key & 0xFFFF);
				nearest = Math.min(nearest, Math.sqrt(ddx * ddx + ddy * ddy));
			}
			double t = Math.min(nearest / (reach + 1), 1.0);
			int ground = upperHeights[x][y];
			entry.setValue((int) Math.round(deckHeight + (ground - deckHeight) * t));
		}
		return result;
	}
}
