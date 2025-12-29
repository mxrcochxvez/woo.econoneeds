# Econoneeds

A lightweight economy plugin for Spigot/Paper Minecraft servers (1.21.1+).

## Features

- 💰 **Player Balance System** — Track and manage player currency
- 💸 **Player-to-Player Payments** — Send money to other players
- 🏆 **Leaderboard** — View the richest players on the server
- 🔧 **Admin Tools** — Give, take, set, and check player balances
- 💾 **YAML Storage** — Simple file-based economy data persistence

## Commands

All economy functionality is accessed through the unified `/eco` command (aliases: `/economy`, `/money`).

### Player Commands

| Command | Description |
|---------|-------------|
| `/eco bal` | Check your balance |
| `/eco pay <player> <amount>` | Send money to another player |
| `/eco top` | View the richest players |

### Admin Commands

> **Note:** Requires `econoneeds.admin` permission (OP by default)

| Command | Description |
|---------|-------------|
| `/eco give <player> <amount>` | Give money to a player |
| `/eco take <player> <amount>` | Take money from a player |
| `/eco set <player> <amount>` | Set a player's balance |
| `/eco check <player>` | Check a player's balance |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `econoneeds.admin` | Access to admin economy commands (give, take, set, check) | OP |

## Installation

1. Download the latest release JAR
2. Place it in your server's `plugins/` folder
3. Restart the server

## Building from Source

### Requirements

- Java 21+
- Gradle

### Build Commands

```bash
# Build the plugin
./gradlew build

# Build and deploy to plugins folder (update path in build.gradle)
./gradlew deploy
```

The compiled JAR will be in `build/libs/`.

## Data Storage

Economy data is stored in `plugins/Econoneeds/economy.yml` using player UUIDs for persistence across name changes.

## License

MIT

## Author

World of Orbis
