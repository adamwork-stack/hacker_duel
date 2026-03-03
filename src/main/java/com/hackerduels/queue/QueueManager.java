package com.hackerduels.queue;

import com.hackerduels.HackerDuelsPlugin;
import com.hackerduels.arena.AnticheatType;
import com.hackerduels.config.PluginConfig;
import com.hackerduels.duel.DuelManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager {

    private final HackerDuelsPlugin plugin;
    private final DuelManager duelManager;
    private final PluginConfig config;

    private final Map<UUID, DuelRequest> pendingChallenges = new ConcurrentHashMap<>();
    private final Map<AnticheatType, ArrayDeque<UUID>> matchmakingQueues = new ConcurrentHashMap<>();
    private BukkitTask matchmakingTask;
    private BukkitTask expireTask;

    public QueueManager(HackerDuelsPlugin plugin, DuelManager duelManager, PluginConfig config) {
        this.plugin = plugin;
        this.duelManager = duelManager;
        this.config = config;
        for (AnticheatType type : AnticheatType.values()) {
            matchmakingQueues.put(type, new ArrayDeque<>());
        }
    }

    public void start() {
        int interval = config.getMatchmakingCheckInterval();
        matchmakingTask = new BukkitRunnable() {
            @Override
            public void run() {
                runMatchmaking();
            }
        }.runTaskTimer(plugin, interval * 20L, interval * 20L);

        expireTask = new BukkitRunnable() {
            @Override
            public void run() {
                expireChallenges();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void stop() {
        if (matchmakingTask != null) matchmakingTask.cancel();
        if (expireTask != null) expireTask.cancel();
        pendingChallenges.clear();
        for (ArrayDeque<UUID> q : matchmakingQueues.values()) {
            q.clear();
        }
    }

    public boolean sendChallenge(Player challenger, Player target, AnticheatType type) {
        if (hasPendingChallenge(challenger) || hasPendingChallenge(target)) return false;
        if (isInQueue(challenger) || isInQueue(target)) return false;
        if (duelManager.isInDuel(challenger.getUniqueId()) || duelManager.isInDuel(target.getUniqueId())) return false;

        DuelRequest req = new DuelRequest(challenger, target, type, config.getQueueTimeout());
        pendingChallenges.put(challenger.getUniqueId(), req);
        pendingChallenges.put(target.getUniqueId(), req);
        return true;
    }

    public DuelRequest getPendingChallenge(UUID player) {
        DuelRequest req = pendingChallenges.get(player);
        if (req != null && req.isExpired()) {
            removeChallenge(req);
            return null;
        }
        return req;
    }

    public boolean hasPendingChallenge(UUID player) {
        return getPendingChallenge(player) != null;
    }

    public void removeChallenge(DuelRequest req) {
        pendingChallenges.remove(req.getChallenger());
        pendingChallenges.remove(req.getTarget());
    }

    private void expireChallenges() {
        var toRemove = new java.util.ArrayList<DuelRequest>();
        for (DuelRequest req : pendingChallenges.values()) {
            if (req.isExpired()) toRemove.add(req);
        }
        for (DuelRequest req : toRemove) {
            removeChallenge(req);
            Player challenger = Bukkit.getPlayer(req.getChallenger());
            if (challenger != null) challenger.sendMessage(config.getMessage("challenge_expired"));
        }
    }

    public boolean joinQueue(Player player, AnticheatType type) {
        if (hasPendingChallenge(player.getUniqueId())) return false;
        if (duelManager.isInDuel(player.getUniqueId())) return false;
        ArrayDeque<UUID> q = matchmakingQueues.get(type);
        if (q.contains(player.getUniqueId())) return false;
        q.add(player.getUniqueId());
        return true;
    }

    public boolean leaveQueue(Player player) {
        for (ArrayDeque<UUID> q : matchmakingQueues.values()) {
            if (q.remove(player.getUniqueId())) return true;
        }
        return false;
    }

    public boolean isInQueue(Player player) {
        return isInQueue(player.getUniqueId());
    }

    public boolean isInQueue(UUID uuid) {
        for (ArrayDeque<UUID> q : matchmakingQueues.values()) {
            if (q.contains(uuid)) return true;
        }
        return false;
    }

    private void runMatchmaking() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (AnticheatType type : AnticheatType.values()) {
                ArrayDeque<UUID> q = matchmakingQueues.get(type);
                while (q.size() >= 2) {
                    UUID p1 = q.poll();
                    UUID p2 = q.poll();
                    if (p1 == null || p2 == null) break;
                    Player pl1 = Bukkit.getPlayer(p1);
                    Player pl2 = Bukkit.getPlayer(p2);
                    if (pl1 == null || !pl1.isOnline() || pl2 == null || !pl2.isOnline()) continue;
                    if (duelManager.isInDuel(p1) || duelManager.isInDuel(p2)) continue;
                    if (duelManager.startDuel(pl1, pl2, type)) {
                        pl1.sendMessage(config.getMessage("duel_start"));
                        pl2.sendMessage(config.getMessage("duel_start"));
                    } else {
                        q.addFirst(p2);
                        q.addFirst(p1);
                    }
                }
            }
        });
    }
}
