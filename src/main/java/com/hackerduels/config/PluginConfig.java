package com.hackerduels.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class PluginConfig {

    private final FileConfiguration config;
    private final Map<String, String> messages = new HashMap<>();

    public PluginConfig(FileConfiguration config) {
        this.config = config;
        loadMessages();
    }

    public void loadMessages() {
        messages.clear();
        var messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                messages.put(key, colorize(messagesSection.getString(key, "")));
            }
        }
    }

    public int getArenaCount(String type) {
        return config.getInt("arenas." + type, 4);
    }

    public int getQueueTimeout() {
        return config.getInt("queue_timeout", 30);
    }

    public int getMatchmakingCheckInterval() {
        return config.getInt("matchmaking_check_interval", 2);
    }

    public String getReturnSpawnWorld() {
        return config.getString("return_spawn_world", "");
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "&cMissing message: " + key);
    }

    public String getMessage(String key, Map<String, String> replacements) {
        String msg = getMessage(key);
        for (Map.Entry<String, String> e : replacements.entrySet()) {
            msg = msg.replace("%" + e.getKey() + "%", e.getValue());
        }
        return msg;
    }

    private static String colorize(String s) {
        if (s == null) return "";
        return s.replace("&", "\u00a7");
    }
}
