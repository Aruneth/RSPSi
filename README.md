# RSPSi — OSRS Map Editor

A JavaFX-based map editor for Old School RuneScape caches. RSPSi uses a plugin architecture to support different cache formats; the included **OSRSPlugin** targets the live OSRS cache.

> This repository is a fork of [blurite/RSPSi](https://github.com/blurite/RSPSi) with additional cache-format fixes and rev 239 decoder corrections.

---

## Requirements

| Tool | Version |
|------|---------|
| JDK  | 21      |
| Gradle | 8.14.3 (via wrapper — no local install needed) |

JavaFX 17 is pulled in automatically as a Gradle dependency; you do not need to install it separately.

---

## Project structure

```
RSPSi/
├── Client/          # Core game engine and plugin API
├── Editor/          # JavaFX application (launcher + main window)
│   └── plugins/
│       ├── active/      # Plugins loaded on startup
│       └── inactive/    # Installed but disabled plugins
└── Plugins/
    └── OSRSPlugin/  # OSRS cache loader (ClientPlugin implementation)
```

### Modules

| Module | Description |
|--------|-------------|
| `Client` | Cache infrastructure, `Buffer`, `ObjectDefinition`, and the two plugin interfaces |
| `Editor` | JavaFX launcher and editor UI; loads plugins from `Editor/plugins/active/` |
| `Plugins:OSRSPlugin` | Loads OSRS object-, floor-, animation-, texture-, and map-data from a cache |

---

## Building

```bash
# Build the entire project
./gradlew build

# Run the editor directly (no installer needed)
./gradlew :Editor:run

# Build only the OSRSPlugin JAR and copy it to Editor/plugins/inactive/
./gradlew :Plugins:OSRSPlugin:buildAndMove

# Create a platform-specific installer (MSI / DEB / RPM / PKG)
./gradlew :Editor:jpackage
```

All build artefacts land in each module's `build/libs/` directory:

| Artefact | Path |
|----------|------|
| Editor application | `Editor/build/libs/Editor.jar` |
| OSRSPlugin | `Plugins/OSRSPlugin/build/libs/OSRSPlugin-1.8.0-BETA.jar` |

---

## Running

1. Build or download the Editor JAR.
2. Make sure at least one plugin JAR is in `Editor/plugins/active/`.
3. Launch:
   ```bash
   ./gradlew :Editor:run
   ```
   Or, from the `Editor/` directory after a full build:
   ```bash
   java -jar build/libs/Editor.jar
   ```
4. The **launcher window** appears. It lists active and inactive plugins; you can toggle them before clicking **Launch**.
5. Point the editor at an OSRS cache directory when prompted.

---

## Plugin system

RSPSi discovers plugins at runtime using Java's `ServiceLoader`. Two plugin interfaces are available:

### `ClientPlugin` — cache loaders

Defined in `Client/src/main/java/com/rspsi/plugins/core/ClientPlugin.java`.

```java
public interface ClientPlugin {
    void initializePlugin();
    void onGameLoaded(Client client) throws Exception;
    default void onResourceDelivered(ResourceResponse resource) { }
}
```

Implement this to register custom definition loaders (objects, floors, animations, textures, etc.).
The OSRSPlugin is the reference implementation.

### `ApplicationPlugin` — UI extensions

Defined in `Editor/src/main/java/com/rspsi/plugins/ui/ApplicationPlugin.java`.

```java
public interface ApplicationPlugin {
    void initialize(MainWindow window);
}
```

Implement this to add panels, menus, or other UI components to the editor window.

---

## Building a plugin

### 1. Create a Gradle subproject under `Plugins/`

Add it to `settings.gradle`:

```groovy
include ':Plugins:MyPlugin'
```

Create `Plugins/MyPlugin/build.gradle`:

```groovy
plugins {
    id 'java'
    id 'com.github.harbby.gradle.serviceloader' version '1.1.9'
}

group   = 'com.example'
version = '1.0.0'

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation project(':Client')   // gives you the plugin interfaces + cache types
    // implementation project(':Editor') // only needed for ApplicationPlugin
}

serviceLoader {
    serviceInterface 'com.rspsi.plugins.core.ClientPlugin'
    // serviceInterface 'com.rspsi.plugins.ui.ApplicationPlugin'
}

// Copy the built JAR to the editor's inactive plugins folder
task buildAndMove(type: Copy, dependsOn: jar) {
    from jar
    into rootProject.file('Editor/plugins/inactive')
}
```

### 2. Implement the interface

```java
package com.example.myplugin;

import com.rspsi.plugins.core.ClientPlugin;
import com.jagex.Client;

public class MyPlugin implements ClientPlugin {

    @Override
    public void initializePlugin() {
        // Register your loaders here before the cache is loaded
    }

    @Override
    public void onGameLoaded(Client client) throws Exception {
        // Access cache archives through client and initialise loaders
    }
}
```

### 3. Register the service provider

The `serviceloader` Gradle plugin generates
`META-INF/services/com.rspsi.plugins.core.ClientPlugin` automatically during the build —
you do not need to create it by hand.

### 4. Build and install

```bash
./gradlew :Plugins:MyPlugin:buildAndMove
```

This compiles the plugin and copies the JAR to `Editor/plugins/inactive/`.

### 5. Activate the plugin

Either:
- Move the JAR from `Editor/plugins/inactive/` to `Editor/plugins/active/` manually, **or**
- Start the editor and use the launcher UI to enable it there.

The editor loads all JARs from `Editor/plugins/active/` on every startup.

---

## Plugin directory reference

| Directory | Purpose |
|-----------|---------|
| `Editor/plugins/active/` | Loaded on every startup; put production plugins here |
| `Editor/plugins/inactive/` | Installed but disabled; `buildAndMove` targets this folder |

Plugins are plain JAR files. Moving a JAR between the two directories is all that is needed to enable or disable it — no configuration files required.

---

## Cache compatibility

| Plugin | Supported revision |
|--------|--------------------|
| OSRSPlugin | OSRS rev 237 – 239 |

The `ObjectDefinitionLoaderOSRS` has been verified against RuneLite's cache module through rev 239. Item and world-map loaders changed in 238–239 but are not used by this editor.

---

## License

MIT — see [LICENSE](LICENSE).
