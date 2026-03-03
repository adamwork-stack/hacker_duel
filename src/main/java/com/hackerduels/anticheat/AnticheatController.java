package com.hackerduels.anticheat;

import com.hackerduels.arena.AnticheatType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controls anticheat bypass per player.
 * For NONE (No AC) duels: grants grim.exempt via PermissionAttachment.
 * For GRIM duels: no bypass - Grim runs normally.
 */
public class AnticheatController {

    private static final String GRIM_EXEMPT_PERMISSION = "grim.exempt";

    private final JavaPlugin plugin;
    private final Map<UUID, PermissionAttachment> bypassAttachments = new ConcurrentHashMap<>();
    private boolean grimAvailable;

    public AnticheatController(JavaPlugin plugin) {
        this.plugin = plugin;
        this.grimAvailable = Bukkit.getPluginManager().getPlugin("GrimAC") != null;
    }

    public boolean isGrimAvailable() {
        return grimAvailable;
    }

    /**
     * Call when a player enters a duel. Applies bypass for NONE type.
     */
    public void onPlayerEnterDuel(Player player, AnticheatType type) {
        if (type == AnticheatType.NONE && grimAvailable) {
            addBypass(player);
        }
    }

    /**
     * Call when a player leaves a duel. Removes bypass.
     */
    public void onPlayerLeaveDuel(Player player) {
        removeBypass(player);
    }

    private void addBypass(Player player) {
        removeBypass(player);
        PermissionAttachment att = player.addAttachment(plugin);
        att.setPermission(GRIM_EXEMPT_PERMISSION, true);
        bypassAttachments.put(player.getUniqueId(), att);
    }

    private void removeBypass(Player player) {
        PermissionAttachment att = bypassAttachments.remove(player.getUniqueId());
        if (att != null) {
            player.removeAttachment(att);
        }
    }

    public void cleanup() {
        for (Map.Entry<UUID, PermissionAttachment> e : bypassAttachments.entrySet()) {
            Player p = Bukkit.getPlayer(e.getKey());
            if (p != null && p.isOnline()) {
                p.removeAttachment(e.getValue());
            }
        }
        bypassAttachments.clear();
    }
}
