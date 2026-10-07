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

## Mountain tool

The Mountain tool raises the terrain into a hill, mountain or mountain range. Select the **MTN** button in the toolbar;
the settings are in the **Mountain** tab on the right. The mountain is added on top of the existing terrain and fades
out into it. The overlays and objects are not changed.

- **Hill / mountain**: click the centre of the mountain. With tiles selected, press **Enter** to raise the selected
  area instead (highest in the middle).
- **Mountain range**: click points along the ridge, **Enter** applies it, **Esc** cancels it.
- Settings: height, radius (or width of a range), steepness, foot blend, irregularity of the outline, roughness,
  peak variation along a range and a seed. The same seed and settings give the same mountain.
- One undo step per mountain.

## Modify Height tool

Select the **H_M** button in the toolbar and hold the left mouse button while moving over the terrain. The brush
(size and shape from the brush settings) changes the height of every tile it touches, on the height level that is
currently selected. What it does depends on the key you hold:

| Keys            | Effect                                                                                   |
|-----------------|------------------------------------------------------------------------------------------|
| *(none)*        | **Raise** the terrain a little every time the brush updates; hold the button to keep raising. |
| `Shift`         | **Lower** the terrain in the same way.                                                   |
| `Ctrl`          | **Smooth**: every tile becomes the average of its 3x3 neighbours. Use it to even out bumps and steps. |
| `Alt`           | **Set** the tile to the value of the *Tile Height* slider (see below).                   |
| `Shift` + `Alt` | **Set** the tile to the *Tile Height* slider value, always measured from the ground (0). |

Tips:
- To flatten an area, use `Alt` (or `Shift` + `Alt`) with the same slider value on the whole area, then use `Ctrl`
  on the edges.
- To blend two regions of a different height, paint over the border between them with `Ctrl`. Every pass moves the
  tiles closer to their neighbours, so move over the border several times, or use a bigger brush, for a gentler slope.
- Ground can never go below 0; heights higher than the tile below are corrected for you.

Details:
- **Tile Height slider** (right hand panel): the height used by `Alt` and `Shift` + `Alt`.
- **Absolute** checkbox: with `Alt` on a height level above 0, the slider value is added to the tile on the level
  below (relative, the default) or measured from 0 (absolute). `Shift` + `Alt` is always absolute.
- Changes apply to the selected height level and to all levels above it, so objects and floors stay aligned. The
  levels above are also kept from dipping below the level underneath.
- Every stroke can be undone with undo (`Ctrl` + `Z`).
- Other height actions are in the menu: *Copy heights from tile*, *Set heights to tiles* (for a whole selection),
  *Set Tile Height* and the bridge generator.

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
