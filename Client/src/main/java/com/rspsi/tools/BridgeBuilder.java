package com.rspsi.tools;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jagex.Client;
import com.jagex.Client.LoadState;
import com.jagex.map.SceneGraph;
import com.jagex.map.tile.SceneTile;
import com.rspsi.options.Options;

/**
 * Builds a bridge over the selected tiles: the deck is laid on the level above the current height level, with a ramp
 * around it. See {@link BridgeShaper} for the shape and {@link SceneGraph#applyBridge} for writing it to the map.
 */
public class BridgeBuilder {

	/** What the bridge looks like; the shape of the deck and ramp is in {@link BridgeShaper.Params}. */
	public static class Settings {
		public final BridgeShaper.Params shape = new BridgeShaper.Params();
		/** Overlay for the deck; 0 keeps whatever is there. */
		public int overlayId = 1;
		public int overlayShape = 1;
		public boolean bridgeFlag = true;
	}

	/** Checks that a bridge can be built; returns the reason in plain words, or null when everything is fine. */
	public static String problem() {
		Client client = Client.getSingleton();
		if (client == null || client.loadState != LoadState.ACTIVE)
			return "Open a map first.";
		if (Options.currentHeight.get() >= 3)
			return "A bridge is built on the level above the current one, so the current height level can be at most 2.";
		if (client.sceneGraph.getSelectedTiles().isEmpty())
			return "Select the tiles of the bridge deck first (select tile tool), on the height level the bridge crosses.";
		return null;
	}

	/** Builds the bridge on the next render cycle, as one undo step. */
	public static void buildBridge(Settings settings) throws Exception {
		String problem = problem();
		if (problem != null)
			throw new Exception(problem);

		Client client = Client.getSingleton();
		int lowerPlane = Options.currentHeight.get();
		List<SceneTile> selected = client.sceneGraph.getSelectedTiles();
		Set<Integer> deck = new HashSet<>();
		for (SceneTile tile : selected)
			deck.add(BridgeShaper.key(tile.positionX, tile.positionY));

		SceneGraph.onCycleEnd.add(() -> {
			client.sceneGraph.applyBridge(deck, lowerPlane, settings.shape, settings.overlayId, settings.overlayShape,
					settings.bridgeFlag);
			client.sceneGraph.resetTiles();
		});
	}

}
