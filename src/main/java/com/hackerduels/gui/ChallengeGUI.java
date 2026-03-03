package com.hackerduels.gui;

import com.hackerduels.HackerDuelsPlugin;
import com.hackerduels.duel.DuelManager;
import com.hackerduels.queue.DuelRequest;
import com.hackerduels.queue.QueueManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ChallengeGUI implements Listener {

    private static final String TITLE = "Duel Challenge";
    private static final int SLOT_ACCEPT = 2;
    private static final int SLOT_DECLINE = 6;

    private final HackerDuelsPlugin plugin;
    private final QueueManager queueManager;
    private final DuelManager duelManager;

    public ChallengeGUI(HackerDuelsPlugin plugin, QueueManager queueManager, DuelManager duelManager) {
        this.plugin = plugin;
        this.queueManager = queueManager;
        this.duelManager = duelManager;
    }

    public void open(Player target, DuelRequest request) {
        Player challenger = Bukkit.getPlayer(request.getChallenger());
        String challengerName = challenger != null ? challenger.getName() : "Unknown";
        String acName = request.getAnticheatType().getDisplayName();

        Inventory inv = Bukkit.createInventory(new ChallengeHolder(), 9, Component.text(TITLE));

        ItemStack accept = new ItemStack(Material.LIME_WOOL);
        accept.editMeta(meta -> {
            meta.displayName(Component.text("Accept").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
        });
        inv.setItem(SLOT_ACCEPT, accept);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(Bukkit.getOfflinePlayer(request.getChallenger())));
        head.editMeta(meta -> meta.displayName(
                Component.text(challengerName + " - " + acName).color(NamedTextColor.YELLOW)));
        inv.setItem(4, head);

        ItemStack decline = new ItemStack(Material.RED_WOOL);
        decline.editMeta(meta -> {
            meta.displayName(Component.text("Decline").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
        });
        inv.setItem(SLOT_DECLINE, decline);

        target.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTopInventory().getHolder() == null
                || !(e.getView().getTopInventory().getHolder() instanceof ChallengeHolder)) return;

        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player player)) return;

        DuelRequest req = queueManager.getPendingChallenge(player.getUniqueId());
        if (req == null) {
            player.closeInventory();
            return;
        }

        int slot = e.getRawSlot();
        if (slot == SLOT_ACCEPT) {
            queueManager.removeChallenge(req);
            player.closeInventory();
            Player challenger = Bukkit.getPlayer(req.getChallenger());
            if (challenger != null && challenger.isOnline()) {
                duelManager.startDuel(challenger, player, req.getAnticheatType());
                String msg = plugin.getPluginConfig().getMessage("challenge_accepted",
                        java.util.Map.of("player", player.getName()));
                challenger.sendMessage(msg);
            }
        } else if (slot == SLOT_DECLINE) {
            queueManager.removeChallenge(req);
            player.closeInventory();
            Player challenger = Bukkit.getPlayer(req.getChallenger());
            if (challenger != null) {
                challenger.sendMessage(plugin.getPluginConfig().getMessage("challenge_declined",
                        java.util.Map.of("player", player.getName())));
            }
        }
    }

    private static class ChallengeHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
