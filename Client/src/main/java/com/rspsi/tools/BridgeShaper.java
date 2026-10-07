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
