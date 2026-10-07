package com.rspsi.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class BridgeShaperTest {

	private static int[][] flat(int height) {
		int[][] h = new int[20][20];
		for (int[] row : h)
			Arrays.fill(row, height);
		return h;
	}

	private static Set<Integer> deck(int x0, int x1, int y) {
		Set<Integer> d = new HashSet<>();
		for (int x = x0; x <= x1; x++)
			d.add(BridgeShaper.key(x, y));
		return d;
	}

	@Test
	void deckUsesHighestPointUnderneath() {
		int[][] lower = flat(-40);
		lower[6][10] = -120;
		BridgeShaper.Result r = BridgeShaper.shape(deck(5, 8, 10), lower, flat(-40), new BridgeShaper.Params());
		for (int h : r.deck.values())
			assertEquals(-120, h);
		assertEquals(4, r.deck.size());
	}

	@Test
	void fixedDeckHeightOverridesAuto() {
		BridgeShaper.Params p = new BridgeShaper.Params();
		p.deckHeight = -200;
		BridgeShaper.Result r = BridgeShaper.shape(deck(5, 8, 10), flat(-40), flat(-40), p);
		assertEquals(-200, r.deck.get(BridgeShaper.key(5, 10)));
	}

	@Test
	void rampSlopesDownGradually() {
		BridgeShaper.Params p = new BridgeShaper.Params();
		p.rampWidth = 3;
		BridgeShaper.Result r = BridgeShaper.shape(deck(5, 8, 10), flat(-100), flat(0), p);
		int one = r.ramp.get(BridgeShaper.key(4, 10));
		int two = r.ramp.get(BridgeShaper.key(3, 10));
		int three = r.ramp.get(BridgeShaper.key(2, 10));
		assertTrue(one < two && two < three && three < 0, "heights should fall away from the deck: " + one + " " + two + " " + three);
		assertFalse(r.ramp.containsKey(BridgeShaper.key(1, 10)), "nothing beyond the ramp width");
	}

	@Test
	void rampNeverIncludesDeckTiles() {
		BridgeShaper.Result r = BridgeShaper.shape(deck(5, 8, 10), flat(-100), flat(0), new BridgeShaper.Params());
		for (int key : r.deck.keySet())
			assertFalse(r.ramp.containsKey(key));
	}

	@Test
	void zeroRampGivesNoRampTilesBeyondDeck() {
		BridgeShaper.Params p = new BridgeShaper.Params();
		p.rampWidth = 0;
		BridgeShaper.Result r = BridgeShaper.shape(deck(5, 8, 10), flat(-100), flat(0), p);
		assertTrue(r.ramp.isEmpty());
	}

	@Test
	void mapEdgeDoesNotThrow() {
		Set<Integer> d = deck(0, 2, 0);
		BridgeShaper.Result r = BridgeShaper.shape(d, flat(-100), flat(0), new BridgeShaper.Params());
		assertFalse(r.ramp.isEmpty());
		for (int key : r.ramp.keySet()) {
			assertTrue((key >> 16) >= 0 && (key & 0xFFFF) >= 0);
		}
		BridgeShaper.Result right = BridgeShaper.shape(deck(17, 19, 19), flat(-100), flat(0), new BridgeShaper.Params());
		for (int key : right.ramp.keySet()) {
			assertTrue((key >> 16) < 20 && (key & 0xFFFF) < 20);
		}
	}

	@Test
	void deckTilesFollowTheLineWithTheGivenWidth() {
		java.util.List<double[]> line = Arrays.asList(new double[] { 2.5, 10.5 }, new double[] { 12.5, 10.5 });
		Set<Integer> three = BridgeShaper.deckTiles(line, 3, 20, 20);
		assertTrue(three.contains(BridgeShaper.key(7, 9)) && three.contains(BridgeShaper.key(7, 10))
				&& three.contains(BridgeShaper.key(7, 11)));
		assertFalse(three.contains(BridgeShaper.key(7, 12)));
		assertFalse(three.contains(BridgeShaper.key(14, 10)), "stops at the end of the line");
		assertFalse(three.contains(BridgeShaper.key(1, 10)), "no cap before the first point");
		assertEquals(33, three.size());
		assertEquals(11, BridgeShaper.deckTiles(line, 1, 20, 20).size());
	}

	@Test
	void deckTilesStayInsideTheMap() {
		java.util.List<double[]> line = Arrays.asList(new double[] { 0.5, 0.5 }, new double[] { 19.5, 0.5 });
		for (int key : BridgeShaper.deckTiles(line, 5, 20, 20))
			assertTrue((key >> 16) >= 0 && (key >> 16) < 20 && (key & 0xFFFF) >= 0 && (key & 0xFFFF) < 20);
	}

	@Test
	void emptyDeckGivesEmptyResult() {
		BridgeShaper.Result r = BridgeShaper.shape(new HashSet<>(), flat(0), flat(0), new BridgeShaper.Params());
		assertTrue(r.deck.isEmpty() && r.ramp.isEmpty());
	}
}
