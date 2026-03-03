package com.hackerduels.arena;

import org.bukkit.Location;
import org.bukkit.World;

public class Arena {

    private final String id;
    private final AnticheatType type;
    private Location spawn1;
    private Location spawn2;
    private boolean inUse;

    public Arena(String id, AnticheatType type) {
        this.id = id;
        this.type = type;
    }

    public Arena(String id, AnticheatType type, Location spawn1, Location spawn2) {
        this.id = id;
        this.type = type;
        this.spawn1 = spawn1;
        this.spawn2 = spawn2;
    }

    public String getId() {
        return id;
    }

    public AnticheatType getType() {
        return type;
    }

    public Location getSpawn1() {
        return spawn1;
    }

    public void setSpawn1(Location spawn1) {
        this.spawn1 = spawn1;
    }

    public Location getSpawn2() {
        return spawn2;
    }

    public void setSpawn2(Location spawn2) {
        this.spawn2 = spawn2;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public boolean isReady() {
        return spawn1 != null && spawn2 != null && spawn1.getWorld() != null && spawn2.getWorld() != null;
    }

    public World getWorld() {
        return spawn1 != null ? spawn1.getWorld() : null;
    }
}
