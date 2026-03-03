package com.hackerduels.queue;

import com.hackerduels.arena.AnticheatType;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DuelRequest {

    private final UUID challenger;
    private final UUID target;
    private final AnticheatType anticheatType;
    private final long expiresAt;

    public DuelRequest(Player challenger, Player target, AnticheatType anticheatType, int timeoutSeconds) {
        this.challenger = challenger.getUniqueId();
        this.target = target.getUniqueId();
        this.anticheatType = anticheatType;
        this.expiresAt = System.currentTimeMillis() + (timeoutSeconds * 1000L);
    }

    public UUID getChallenger() {
        return challenger;
    }

    public UUID getTarget() {
        return target;
    }

    public AnticheatType getAnticheatType() {
        return anticheatType;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
