package com.hackerduels.duel;

import com.hackerduels.HackerDuelsPlugin;
import com.hackerduels.anticheat.AnticheatController;
import com.hackerduels.arena.Arena;
import com.hackerduels.arena.ArenaManager;
import com.hackerduels.arena.AnticheatType;
import com.hackerduels.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DuelManager implements Listener {

    private final HackerDuelsPlugin plugin;
    private final ArenaManager arenaManager;
    private final AnticheatController anticheatController;
    private final PluginConfig config;

    private final Map<UUID, Duel> activeDuels = new ConcurrentHashMap<>();
    private BukkitTask cleanupTask;

    public DuelManager(HackerDuelsPlugin plugin, ArenaManager arenaManager,
                       AnticheatController anticheatController, PluginConfig config) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.anticheatController = anticheatController;
        this.config = config;
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void stop() {
        HandlerList.unregisterAll(this);
        if (cleanupTask != null) cleanupTask.cancel();
        for (Duel duel : activeDuels.values()) {
            endDuel(duel, null, false);
        }
        activeDuels.clear();
    }

    public boolean startDuel(Player p1, Player p2, AnticheatType type) {
        var arenaOpt = arenaManager.allocateArena(type);
        if (arenaOpt.isEmpty()) {
            return false;
        }
        Arena arena = arenaOpt.get();
        Location ret1 = p1.getLocation().clone();
        Location ret2 = p2.getLocation().clone();

        Duel duel = new Duel(p1, p2, type, arena, ret1, ret2);
        activeDuels.put(p1.getUniqueId(), duel);
        activeDuels.put(p2.getUniqueId(), duel);

        anticheatController.onPlayerEnterDuel(p1, type);
        anticheatController.onPlayerEnterDuel(p2, type);

        p1.teleport(arena.getSpawn1());
        p2.teleport(arena.getSpawn2());
        p1.getInventory().clear();
        p2.getInventory().clear();
        p1.setHealth(p1.getMaxHealth());
        p2.setHealth(p2.getMaxHealth());
        p1.setFoodLevel(20);
        p2.setFoodLevel(20);

        String msg = config.getMessage("duel_start");
        p1.sendMessage(msg);
        p2.sendMessage(msg);

        return true;
    }

    private void endDuel(Duel duel, UUID winner, boolean sendMessages) {
        Player p1 = Bukkit.getPlayer(duel.getPlayer1());
        Player p2 = Bukkit.getPlayer(duel.getPlayer2());

        anticheatController.onPlayerLeaveDuel(p1);
        anticheatController.onPlayerLeaveDuel(p2);

        if (sendMessages && winner != null) {
            Player winnerP = Bukkit.getPlayer(winner);
            Player loserP = Bukkit.getPlayer(duel.getOpponent(winner));
            if (winnerP != null) winnerP.sendMessage(config.getMessage("duel_win"));
            if (loserP != null) loserP.sendMessage(config.getMessage("duel_lose"));
        }

        Location ret1 = applyReturnWorld(duel.getPlayer1ReturnLoc());
        Location ret2 = applyReturnWorld(duel.getPlayer2ReturnLoc());
        if (ret1 != null && ret1.getWorld() != null && p1 != null && p1.isOnline()) {
            p1.teleport(ret1);
            p1.getInventory().clear();
            p1.setHealth(p1.getMaxHealth());
            p1.setFoodLevel(20);
        }
        if (ret2 != null && ret2.getWorld() != null && p2 != null && p2.isOnline()) {
            p2.teleport(ret2);
            p2.getInventory().clear();
            p2.setHealth(p2.getMaxHealth());
            p2.setFoodLevel(20);
        }

        activeDuels.remove(duel.getPlayer1());
        activeDuels.remove(duel.getPlayer2());
        arenaManager.freeArena(duel.getArena());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity();
        Duel duel = activeDuels.get(dead.getUniqueId());
        if (duel == null) return;

        Player killer = dead.getKiller();
        UUID winner = killer != null ? killer.getUniqueId() : duel.getOpponent(dead.getUniqueId());
        endDuel(duel, winner, true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player quitter = e.getPlayer();
        Duel duel = activeDuels.get(quitter.getUniqueId());
        if (duel == null) return;

        UUID winner = duel.getOpponent(quitter.getUniqueId());
        endDuel(duel, winner, true);
    }

    public boolean isInDuel(UUID uuid) {
        return activeDuels.containsKey(uuid);
    }

    public Duel getDuel(UUID uuid) {
        return activeDuels.get(uuid);
    }

    private Location applyReturnWorld(Location loc) {
        if (loc == null) return null;
        String worldName = config.getReturnSpawnWorld();
        if (worldName == null || worldName.isBlank()) return loc;
        var world = Bukkit.getWorld(worldName);
        if (world == null) return loc;
        return world.getSpawnLocation();
    }
}
