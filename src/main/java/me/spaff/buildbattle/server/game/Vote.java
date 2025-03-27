package me.spaff.buildbattle.server.game;

public enum Vote {
    NONE("&f", "None", 0),
    BAD("&c", "Bad", 5),
    GOOD("&2", "Good", 10),
    VERY_GOOD("&a", "Very Good", 15),
    EPIC("&5", "Epic", 20),
    LEGENDARY("&6", "Legendary", 25);

    private final String color;
    private final String name;

    private final int score;

    Vote(String color, String name, int score) {
        this.color = color;
        this.name = name;
        this.score = score;
    }

    public String getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }
}