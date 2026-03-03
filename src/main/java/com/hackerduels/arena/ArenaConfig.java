package com.hackerduels.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArenaConfig {

    private final JavaPlugin plugin;
    private File arenasFile;
    private FileConfiguration arenasConfig;

    public ArenaConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!arenasFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                arenasFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create arenas.yml: " + e.getMessage());
            }
        }
        if (arenasFile.exists()) {
            arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
        } else {
            arenasConfig = new YamlConfiguration();
        }
    }

    public void save() {
        if (arenasConfig != null && arenasFile != null) {
            try {
                arenasConfig.save(arenasFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save arenas.yml: " + e.getMessage());
            }
        }
    }

    public List<Arena> loadArenas(FileConfiguration mainConfig) {
        List<Arena> arenas = new ArrayList<>();
        if (arenasConfig == null) return arenas;

        var loadedIds = new java.util.HashSet<String>();
        for (AnticheatType type : AnticheatType.values()) {
            int count = mainConfig.getInt("arenas." + type.getId(), 4);
            for (int i = 0; i < count; i++) {
                String arenaId = type.getId() + "_" + i;
                loadedIds.add(arenaId);
                Arena arena = loadArena(arenaId, type);
                arenas.add(arena != null ? arena : new Arena(arenaId, type));
            }
        }
        ConfigurationSection arenasSection = arenasConfig.getConfigurationSection("arenas");
        if (arenasSection != null) {
            for (String id : arenasSection.getKeys(false)) {
                if (loadedIds.contains(id)) continue;
                Arena arena = getArenaById(id);
                if (arena != null) arenas.add(arena);
            }
        }
        return arenas;
    }

    private Arena loadArena(String id, AnticheatType type) {
        ConfigurationSection section = arenasConfig.getConfigurationSection("arenas." + id);
        if (section == null) return null;

        Location spawn1 = parseLocation(section.getString("spawn1"));
        Location spawn2 = parseLocation(section.getString("spawn2"));
        if (spawn1 == null || spawn2 == null) return null;

        return new Arena(id, type, spawn1, spawn2);
    }

    private Location parseLocation(String s) {
        if (s == null || s.isBlank()) return null;
        String[] parts = s.split(";");
        if (parts.length < 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length >= 5 ? Float.parseFloat(parts[4]) : 0;
            float pitch = parts.length >= 6 ? Float.parseFloat(parts[5]) : 0;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String locationToString(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ()
                + ";" + loc.getYaw() + ";" + loc.getPitch();
    }

    public void saveArena(Arena arena) {
        if (arenasConfig == null) return;
        String path = "arenas." + arena.getId();
        if (arena.getSpawn1() != null) {
            arenasConfig.set(path + ".spawn1", locationToString(arena.getSpawn1()));
        }
        if (arena.getSpawn2() != null) {
            arenasConfig.set(path + ".spawn2", locationToString(arena.getSpawn2()));
        }
        save();
    }

    public Arena getArenaById(String id) {
        if (arenasConfig == null) return null;
        ConfigurationSection section = arenasConfig.getConfigurationSection("arenas." + id);
        if (section == null) return null;
        String typeId = id.split("_")[0];
        AnticheatType type = AnticheatType.fromId(typeId);
        if (type == null) return null;
        Location spawn1 = parseLocation(section.getString("spawn1"));
        Location spawn2 = parseLocation(section.getString("spawn2"));
        return new Arena(id, type, spawn1, spawn2);
    }
}
