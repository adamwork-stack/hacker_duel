package com.hackerduels;

import com.hackerduels.anticheat.AnticheatController;
import com.hackerduels.arena.ArenaConfig;
import com.hackerduels.arena.ArenaManager;
import com.hackerduels.commands.DuelAdminCommand;
import com.hackerduels.commands.DuelCommand;
import com.hackerduels.config.PluginConfig;
import com.hackerduels.duel.DuelManager;
import com.hackerduels.gui.ChallengeGUI;
import com.hackerduels.queue.QueueManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HackerDuelsPlugin extends JavaPlugin {

    private PluginConfig pluginConfig;
    private ArenaConfig arenaConfig;
    private ArenaManager arenaManager;
    private AnticheatController anticheatController;
    private DuelManager duelManager;
    private QueueManager queueManager;
    private ChallengeGUI challengeGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reload();
        getCommand("duel").setExecutor(new DuelCommand(this, queueManager, challengeGUI));
        getCommand("duel").setTabCompleter((org.bukkit.command.TabCompleter) getCommand("duel").getExecutor());
        getCommand("dueladmin").setExecutor(new DuelAdminCommand(this, arenaManager));
        getCommand("dueladmin").setTabCompleter((org.bukkit.command.TabCompleter) getCommand("dueladmin").getExecutor());
    }

    @Override
    public void onDisable() {
        if (queueManager != null) queueManager.stop();
        if (duelManager != null) duelManager.stop();
        if (anticheatController != null) anticheatController.cleanup();
    }

    public void reload() {
        reloadConfig();
        pluginConfig = new PluginConfig(getConfig());
        if (arenaConfig == null) {
            arenaConfig = new ArenaConfig(this);
        }
        arenaConfig.load();
        if (arenaManager == null) {
            arenaManager = new ArenaManager(this, arenaConfig, pluginConfig);
        }
        arenaManager.loadArenas();
        if (anticheatController == null) {
            anticheatController = new AnticheatController(this);
        }
        if (duelManager == null) {
            duelManager = new DuelManager(this, arenaManager, anticheatController, pluginConfig);
        }
        duelManager.start();
        if (queueManager == null) {
            queueManager = new QueueManager(this, duelManager, pluginConfig);
        }
        queueManager.stop();
        queueManager.start();
        if (challengeGUI == null) {
            challengeGUI = new ChallengeGUI(this, queueManager, duelManager);
            getServer().getPluginManager().registerEvents(challengeGUI, this);
        }
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }
}
