package com.hackerduels.duel;

import com.hackerduels.arena.AnticheatType;
import com.hackerduels.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Duel {

    private final UUID player1;
    private final UUID player2;
    private final AnticheatType anticheatType;
    private final Arena arena;
    private final Location player1ReturnLoc;
    private final Location player2ReturnLoc;
    private final long startTime;

    public Duel(Player p1, Player p2, AnticheatType anticheatType, Arena arena,
                Location p1Return, Location p2Return) {
        this.player1 = p1.getUniqueId();
        this.player2 = p2.getUniqueId();
        this.anticheatType = anticheatType;
        this.arena = arena;
        this.player1ReturnLoc = p1Return;
        this.player2ReturnLoc = p2Return;
        this.startTime = System.currentTimeMillis();
    }

    public UUID getPlayer1() {
        return player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    public AnticheatType getAnticheatType() {
        return anticheatType;
    }

    public Arena getArena() {
        return arena;
    }

    public Location getPlayer1ReturnLoc() {
        return player1ReturnLoc;
    }

    public Location getPlayer2ReturnLoc() {
        return player2ReturnLoc;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean involves(UUID uuid) {
        return player1.equals(uuid) || player2.equals(uuid);
    }

    public UUID getOpponent(UUID uuid) {
        if (player1.equals(uuid)) return player2;
        if (player2.equals(uuid)) return player1;
        return null;
    }
}
