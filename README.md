# Skinnable

A Minecraft mod for Java 26.1.2 that lets you disguise command blocks and mob spawners as any vanilla block. Supports both **Fabric** and **NeoForge** loaders.

## Features

### Skinnable Command Block
- Looks like any vanilla block of your choosing
- **Right-click with a block item** (creative mode) — sets the disguise
- **Right-click with empty hand** (creative mode) — opens the vanilla command block GUI
- Executes commands in Redstone, Auto, or Sequence mode
- Only breakable by players in creative mode

### Skinnable Spawner
- Looks like any vanilla block of your choosing
- **Right-click with a block item** (creative mode) — sets the disguise
- **Right-click with empty hand** (creative mode) — opens the spawner configuration GUI
- Configure via GUI:
  - Mob selection with individual spawn weights (supports all mobs, peaceful and hostile)
  - Minimum and maximum spawn delay
  - Spawn count per cycle
  - Maximum nearby entities
  - Player activation range
- Only breakable by players in creative mode

## Requirements

- **Minecraft** Java 26.1.2
- **Java** 25+
- **Fabric Loader** 0.19.3+ with Fabric API 0.150.0+26.1.2, **or**
- **NeoForge** 26.1.2.75+

## Building

**Windows:**
```bat
set JAVA_HOME=C:\path\to\jdk-25
gradlew.bat build
```

**Linux / macOS:**
```sh
export JAVA_HOME=/path/to/jdk-25
./gradlew build
```

Output JARs are in `fabric/build/libs/` and `neoforge/build/libs/`.

## Project Structure

```
Skinnable/
├── common/          # Shared code (blocks, block entities, networking, client screens)
├── fabric/          # Fabric loader integration
└── neoforge/        # NeoForge loader integration
```

## License

MIT
