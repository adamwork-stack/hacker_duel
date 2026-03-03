# HackerDuels - 1v1 Hacker Duel Plugin

Lightweight Paper plugin for 1v1 duels with anticheat selection (No AC + Grim).

## Requirements

- Paper 1.21.x (or compatible)
- Java 21
- GrimAC (optional, for Grim duels)
- ViaVersion + ViaBackwards (optional, for cross-version clients)

## Building

1. Generate Gradle wrapper (if not present):
   ```
   gradle wrapper
   ```

2. Build:
   ```
   ./gradlew build
   ```
   Or on Windows: `gradlew.bat build`

3. Output: `build/libs/HackerDuels-1.0.0.jar`

## Setup

1. Place the jar in your server's `plugins/` folder
2. Start the server to generate config files
3. Configure arenas:
   - `/dueladmin createarena none_0 none` (creates default arenas from config)
   - Stand at spawn 1, run `/dueladmin setspawn1 none_0`
   - Stand at spawn 2, run `/dueladmin setspawn2 none_0`
   - Repeat for grim_0, grim_1, etc.

## Commands

| Command | Description |
|---------|-------------|
| `/duel challenge <player> [grim\|none]` | Challenge a player |
| `/duel queue [grim\|none]` | Join matchmaking |
| `/duel leave` | Leave queue |
| `/duel accept` | Accept challenge |
| `/duel decline` | Decline challenge |
| `/dueladmin createarena <id> <type>` | Create arena |
| `/dueladmin setspawn1 <arenaId>` | Set spawn 1 |
| `/dueladmin setspawn2 <arenaId>` | Set spawn 2 |
| `/dueladmin reload` | Reload config |

## Permissions

- `hackerduels.challenge` - Challenge players
- `hackerduels.queue` - Join matchmaking
- `hackerduels.leave` - Leave queue / decline
- `hackerduels.admin` - Admin commands
