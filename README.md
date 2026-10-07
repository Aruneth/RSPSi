# RSPSi

RSPSi is a desktop map editor for RuneScape private servers. It loads a game cache, renders the world in 3D and lets
you edit the terrain and the objects on it: paint floors, raise and lower the ground, place and remove objects, set
tile flags, and save the result back to your cache or to map files.

This repository is a fork with additional features, most notably the **Path tool** (see below).

## Features

### Map editing
- **Tile selection** with rectangle select, add-to-selection and single tile toggling.
- **Paint overlays and underlays** with an adjustable brush (size and shape: rectangle, circle, checker), all 13
  overlay shapes and rotation. Hold the modifier keys to remove an overlay or to only repaint the overlay colour.
- **Path tool**: draw a path by clicking points and let RSPSi lay the overlay with matching shapes (details below).
- **Modify heights** with a brush, with optional smoothing, relative or absolute height, and multiple height levels.
- **Tile flags**: set unwalkable, bridge, remove roof, render on z - 1 and disable render flags, with overlays to
  visualise them.
- **Objects**: select, spawn and delete objects, with rotation, an object filter and an object preview window.
- **Undo and redo** for tiles and objects.

### Working with tiles
- Copy, paste and delete tile selections, with options for what to include.
- Copy flags, heights, overlays or underlays from one tile to a selection.
- Set heights, flags, overlays and underlays on a whole selection in one action.
- Import a prefab and export selected tiles to reuse them elsewhere.
- Generate bridges, reset tile heights and force a map update.

### Files and tools
- Open a map by coordinates, by hash, from `.dat`/`.gz` files, from `.pack` files or from your cache; save back to the
  same formats.
- A **region editor** and a **map index editor** (under Tools).
- A **remapper** and a **landscape converter** (under Tools).
- Full map view, minimap, object view, adjustable view distance and debug overlays (overlay ids, underlay ids, tile
  heights, FPS and tile information).
- Swatches for overlays, underlays and objects, with reloading of swatches and models.
- Plugin support; the included `OSRSPlugin` adds loaders for Old School RuneScape caches.

## Path tool

The Path tool draws a path for you. Select the **P_P** button in the toolbar, pick an overlay in the Overlay tab, and
click the points of your path in the scene. A preview follows your mouse.

- **Enter** applies the path, **Esc** cancels it.
- RSPSi chooses for every tile the overlay shape and rotation that best matches the path, so the pieces fit together,
  including the curved parts.
- Settings are in the **Path** tab on the right:
  - **Width**: how wide the path is, in tiles.
  - **Smooth curve**: let the path flow in a smooth curve through your points, or use straight lines.
  - **Edge smoothing**: how strongly the shapes of neighbouring tiles are made to line up.
  - **Smooth terrain height** (optional): evens out the terrain under the path so it has no steep steps.
    - **Height smoothing**: number of smoothing passes (1 - 250).
    - **Blend distance**: how many tiles around the path are blended into the surrounding terrain. Use a larger
      distance on steep hillsides.
- Painting the overlay and smoothing the height are separate undo steps.

The width of the right hand panel can be changed by dragging its left edge.

## Building

RSPSi is a Gradle project and needs a JDK 21.

```
./gradlew build
./gradlew :Editor:run
```

On Windows use `gradlew.bat`. The project consists of the `Client` (renderer and cache handling), the `Editor` (the
JavaFX user interface) and the `Plugins` (such as `OSRSPlugin`).

## Credits

RSPSi was created by the RSPSi project and a lot of its code and ideas come from the work of others. Thank you to:

- **[RSPSi/RSPSi](https://github.com/RSPSi/RSPSi)**, the original RSPSi, which this project builds on.
- **[blurite/RSPSi](https://github.com/blurite/RSPSi)**, the continued version of RSPSi that this fork is based on.

## License

RSPSi is released under the MIT License, see [LICENSE](LICENSE).
