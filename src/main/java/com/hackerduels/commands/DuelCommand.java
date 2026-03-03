package com.hackerduels.commands;

import com.hackerduels.HackerDuelsPlugin;
import com.hackerduels.arena.AnticheatType;
import com.hackerduels.gui.ChallengeGUI;
import com.hackerduels.queue.DuelRequest;
import com.hackerduels.queue.QueueManager;
import org.bukkit.Bukkit;
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
import java.util.Map;
import java.util.stream.Collectors;

public class DuelCommand implements CommandExecutor, TabCompleter {

    private final HackerDuelsPlugin plugin;
    private final QueueManager queueManager;
    private final ChallengeGUI challengeGUI;

    public DuelCommand(HackerDuelsPlugin plugin, QueueManager queueManager, ChallengeGUI challengeGUI) {
        this.plugin = plugin;
        this.queueManager = queueManager;
        this.challengeGUI = challengeGUI;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
            case "challenge", "ch" -> handleChallenge(player, args);
            case "queue", "q" -> handleQueue(player, args);
            case "leave", "l" -> handleLeave(player);
            case "accept", "a" -> handleAccept(player);
            case "decline", "d" -> handleDecline(player);
            default -> sendUsage(player);
        }
        return true;
    }

    private void handleChallenge(Player challenger, String[] args) {
        if (!challenger.hasPermission("hackerduels.challenge")) {
            challenger.sendMessage(plugin.getPluginConfig().getMessage("no_permission"));
            return;
        }
        if (args.length < 2) {
            challenger.sendMessage("Usage: /duel challenge <player> [grim|none]");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            challenger.sendMessage(plugin.getPluginConfig().getMessage("player_offline"));
            return;
        }
        if (target.equals(challenger)) {
            challenger.sendMessage(plugin.getPluginConfig().getMessage("challenge_self"));
            return;
        }
        AnticheatType type = args.length >= 3 ? AnticheatType.fromId(args[2]) : AnticheatType.GRIM;
        if (type == null) {
            challenger.sendMessage(plugin.getPluginConfig().getMessage("invalid_anticheat"));
            return;
        }
        if (plugin.getDuelManager().isInDuel(challenger.getUniqueId()) || plugin.getDuelManager().isInDuel(target.getUniqueId())) {
            challenger.sendMessage(plugin.getPluginConfig().getMessage("challenge_in_duel"));
            return;
        }
        if (queueManager.hasPendingChallenge(challenger.getUniqueId()) || queueManager.hasPendingChallenge(target.getUniqueId())) {
            challenger.sendMessage(plugin.getPluginConfig().getMessage("challenge_in_queue"));
            return;
        }
        if (queueManager.isInQueue(challenger) || queueManager.isInQueue(target)) {
            challenger.sendMessage(plugin.getPluginConfig().getMessage("challenge_in_queue"));
            return;
        }
        if (!queueManager.sendChallenge(challenger, target, type)) {
            challenger.sendMessage(plugin.getPluginConfig().getMessage("challenge_in_queue"));
            return;
        }
        challenger.sendMessage(plugin.getPluginConfig().getMessage("challenge_sent",
                Map.of("player", target.getName(), "anticheat", type.getDisplayName())));
        target.sendMessage(plugin.getPluginConfig().getMessage("challenge_received",
                Map.of("player", challenger.getName(), "anticheat", type.getDisplayName())));
        challengeGUI.open(target, queueManager.getPendingChallenge(target.getUniqueId()));
    }

    private void handleQueue(Player player, String[] args) {
        if (!player.hasPermission("hackerduels.queue")) {
            player.sendMessage(plugin.getPluginConfig().getMessage("no_permission"));
            return;
        }
        AnticheatType type = args.length >= 2 ? AnticheatType.fromId(args[1]) : AnticheatType.GRIM;
        if (type == null) {
            player.sendMessage(plugin.getPluginConfig().getMessage("invalid_anticheat"));
            return;
        }
        if (plugin.getDuelManager().isInDuel(player.getUniqueId())) return;
        if (queueManager.hasPendingChallenge(player.getUniqueId())) return;
        if (!queueManager.joinQueue(player, type)) {
            player.sendMessage(plugin.getPluginConfig().getMessage("challenge_in_queue"));
            return;
        }
        player.sendMessage(plugin.getPluginConfig().getMessage("queue_joined",
                Map.of("anticheat", type.getDisplayName())));
    }

    private void handleLeave(Player player) {
        if (!player.hasPermission("hackerduels.leave")) {
            player.sendMessage(plugin.getPluginConfig().getMessage("no_permission"));
            return;
        }
        if (queueManager.leaveQueue(player)) {
            player.sendMessage(plugin.getPluginConfig().getMessage("queue_left"));
        } else {
            player.sendMessage(plugin.getPluginConfig().getMessage("queue_not_in"));
        }
    }

    private void handleAccept(Player player) {
        DuelRequest req = queueManager.getPendingChallenge(player.getUniqueId());
        if (req == null) {
            player.sendMessage(plugin.getPluginConfig().getMessage("no_pending_challenge"));
            return;
        }
        queueManager.removeChallenge(req);
        player.closeInventory();
        Player challenger = Bukkit.getPlayer(req.getChallenger());
        if (challenger != null && challenger.isOnline()) {
            if (plugin.getDuelManager().startDuel(challenger, player, req.getAnticheatType())) {
                challenger.sendMessage(plugin.getPluginConfig().getMessage("challenge_accepted",
                        Map.of("player", player.getName())));
            } else {
                challenger.sendMessage(plugin.getPluginConfig().getMessage("no_arena"));
                player.sendMessage(plugin.getPluginConfig().getMessage("no_arena"));
            }
        }
    }

    private void handleDecline(Player player) {
        if (!player.hasPermission("hackerduels.leave")) {
            player.sendMessage(plugin.getPluginConfig().getMessage("no_permission"));
            return;
        }
        DuelRequest req = queueManager.getPendingChallenge(player.getUniqueId());
        if (req == null) {
            player.sendMessage(plugin.getPluginConfig().getMessage("no_pending_challenge"));
            return;
        }
        queueManager.removeChallenge(req);
        player.closeInventory();
        Player challenger = Bukkit.getPlayer(req.getChallenger());
        if (challenger != null) {
            challenger.sendMessage(plugin.getPluginConfig().getMessage("challenge_declined",
                    Map.of("player", player.getName())));
        }
    }

    private void sendUsage(Player player) {
        player.sendMessage("§e/duel challenge <player> [grim|none] §7- Challenge a player");
        player.sendMessage("§e/duel queue [grim|none] §7- Join matchmaking");
        player.sendMessage("§e/duel leave §7- Leave queue");
        player.sendMessage("§e/duel accept §7- Accept challenge");
        player.sendMessage("§e/duel decline §7- Decline challenge");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (!(sender instanceof Player)) return completions;

        if (args.length == 1) {
            completions.addAll(Arrays.asList("challenge", "queue", "leave", "accept", "decline"));
            return completions.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("challenge")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("queue")) {
                completions.addAll(Arrays.asList("grim", "none"));
                return completions.stream().filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("challenge")) {
                completions.addAll(Arrays.asList("grim", "none"));
                return completions.stream().filter(s -> s.startsWith(args[2].toLowerCase())).collect(Collectors.toList());
            }
        }
        return completions;
    }
}
