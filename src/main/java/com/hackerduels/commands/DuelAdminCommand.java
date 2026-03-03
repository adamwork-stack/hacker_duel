package com.hackerduels.commands;

import com.hackerduels.HackerDuelsPlugin;
import com.hackerduels.arena.AnticheatType;
import com.hackerduels.arena.Arena;
import com.hackerduels.arena.ArenaManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DuelAdminCommand implements CommandExecutor, TabCompleter {

    private final HackerDuelsPlugin plugin;
    private final ArenaManager arenaManager;

    public DuelAdminCommand(HackerDuelsPlugin plugin, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("hackerduels.admin")) {
            sender.sendMessage(plugin.getPluginConfig().getMessage("no_permission"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is for players only.");
            return true;
        }
        if (args.length == 0) {
            sendUsage(player);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "createarena", "create" -> handleCreateArena(player, args);
            case "setspawn1", "spawn1" -> handleSetSpawn(player, args, 1);
            case "setspawn2", "spawn2" -> handleSetSpawn(player, args, 2);
            case "reload" -> handleReload(player);
            default -> sendUsage(player);
        }
        return true;
    }

    private void handleCreateArena(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("Usage: /dueladmin createarena <id> <grim|none>");
            return;
        }
        String id = args[1];
        AnticheatType type = AnticheatType.fromId(args[2]);
        if (type == null) {
            player.sendMessage(plugin.getPluginConfig().getMessage("invalid_anticheat"));
            return;
        }
        arenaManager.createArena(id, type);
        player.sendMessage(plugin.getPluginConfig().getMessage("admin_arena_created",
                java.util.Map.of("id", id, "type", type.getDisplayName())));
    }

    private void handleSetSpawn(Player player, String[] args, int spawnNum) {
        if (args.length < 2) {
            player.sendMessage("Usage: /dueladmin setspawn" + spawnNum + " <arenaId>");
            return;
        }
        String arenaId = args[1];
        Arena arena = arenaManager.getArenaById(arenaId);
        if (arena == null) {
            player.sendMessage(plugin.getPluginConfig().getMessage("admin_arena_not_found"));
            return;
        }
        arenaManager.setArenaSpawn(arenaId, spawnNum, player.getLocation().clone());
        player.sendMessage(plugin.getPluginConfig().getMessage("admin_spawn_set",
                java.util.Map.of("num", String.valueOf(spawnNum), "id", arenaId)));
    }

    private void handleReload(Player player) {
        plugin.reload();
        player.sendMessage(plugin.getPluginConfig().getMessage("admin_reload"));
    }

    private void sendUsage(Player player) {
        player.sendMessage("§e/dueladmin createarena <id> <grim|none> §7- Create arena");
        player.sendMessage("§e/dueladmin setspawn1 <arenaId> §7- Set spawn 1 (stand at location)");
        player.sendMessage("§e/dueladmin setspawn2 <arenaId> §7- Set spawn 2 (stand at location)");
        player.sendMessage("§e/dueladmin reload §7- Reload config");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("hackerduels.admin")) return completions;

        if (args.length == 1) {
            completions.addAll(Arrays.asList("createarena", "setspawn1", "setspawn2", "reload"));
            return completions.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setspawn1") || args[0].equalsIgnoreCase("setspawn2")) {
                return arenaManager.getArenas().stream()
                        .map(Arena::getId)
                        .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("createarena")) {
                return Arrays.asList("none_0", "grim_0");
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("createarena")) {
            completions.addAll(Arrays.asList("grim", "none"));
            return completions.stream().filter(s -> s.startsWith(args[2].toLowerCase())).collect(Collectors.toList());
        }
        return completions;
    }
}
