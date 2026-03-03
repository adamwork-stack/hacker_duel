package com.hackerduels.arena;

import com.hackerduels.config.PluginConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaManager {

    private final JavaPlugin plugin;
    private final ArenaConfig arenaConfig;
    private final PluginConfig pluginConfig;
    private final List<Arena> arenas = new ArrayList<>();
    private final ConcurrentHashMap<String, Arena> arenaById = new ConcurrentHashMap<>();

    public ArenaManager(JavaPlugin plugin, ArenaConfig arenaConfig, PluginConfig pluginConfig) {
        this.plugin = plugin;
        this.arenaConfig = arenaConfig;
        this.pluginConfig = pluginConfig;
    }

    public void loadArenas() {
        arenas.clear();
        arenaById.clear();
        FileConfiguration config = plugin.getConfig();
        List<Arena> loaded = arenaConfig.loadArenas(config);
        for (Arena arena : loaded) {
            arenas.add(arena);
            arenaById.put(arena.getId(), arena);
        }
    }

    public Optional<Arena> allocateArena(AnticheatType type) {
        return arenas.stream()
                .filter(a -> a.getType() == type && !a.isInUse() && a.isReady())
                .findFirst()
                .map(arena -> {
                    arena.setInUse(true);
                    return arena;
                });
    }

    public void freeArena(Arena arena) {
        if (arena != null) {
            arena.setInUse(false);
        }
    }

    public Arena getArenaById(String id) {
        return arenaById.get(id);
    }

    public void createArena(String id, AnticheatType type) {
        Arena arena = new Arena(id, type);
        arenas.add(arena);
        arenaById.put(id, arena);
        arenaConfig.saveArena(arena);
    }

    public void setArenaSpawn(String arenaId, int spawnNum, org.bukkit.Location location) {
        Arena arena = arenaById.get(arenaId);
        if (arena != null) {
            if (spawnNum == 1) arena.setSpawn1(location);
            else if (spawnNum == 2) arena.setSpawn2(location);
            arenaConfig.saveArena(arena);
        }
    }

    public List<Arena> getArenas() {
        return new ArrayList<>(arenas);
    }
}
