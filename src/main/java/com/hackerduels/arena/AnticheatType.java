package com.hackerduels.arena;

import java.util.Locale;

public enum AnticheatType {
    NONE("none", "No Anticheat"),
    GRIM("grim", "Grim");

    private final String id;
    private final String displayName;

    AnticheatType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AnticheatType fromId(String id) {
        if (id == null) return null;
        return switch (id.toLowerCase(Locale.ROOT)) {
            case "none", "no" -> NONE;
            case "grim" -> GRIM;
            default -> null;
        };
    }
}
