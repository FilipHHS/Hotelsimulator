package model;

import java.awt.Color;

public enum AreaType {
    ROOM      ("Room",       "Kamer",      new Color(200, 230, 255)),
    CINEMA    ("Cinema",     "Bioscoop",   new Color(200, 100, 200)),
    RESTAURANT("Restaurant", "Restaurant", new Color(255, 100, 100)),
    FITNESS   ("Fitness",    "Fitness",    new Color(100, 220, 180)),
    LOBBY     ("Lobby",      "Lobby",      new Color(255, 220,  50)),
    SCHACHT   ("Schacht",    "Schacht",    new Color(220, 220, 220)),
    LIFT      ("Lift",       "Lift",       new Color(255, 150,   0)),
    STAIRCASE ("Staircase",  "Trap",       new Color(100, 200, 100)),
    STORAGE   ("Storage",    "Opslag",     new Color(180, 140, 100)),
    UNKNOWN   ("",           "",           Color.WHITE);

    private final String jsonKey;
    private final String label;
    private final Color  color;

    AreaType(String jsonKey, String label, Color color) {
        this.jsonKey = jsonKey;
        this.label   = label;
        this.color   = color;
    }

    public String getLabel() { return label; }
    public Color  getColor() { return color; }

    public static AreaType fromString(String key) {
        for (AreaType type : values()) {
            if (type.jsonKey.equalsIgnoreCase(key)) return type;
        }
        return UNKNOWN;
    }
}