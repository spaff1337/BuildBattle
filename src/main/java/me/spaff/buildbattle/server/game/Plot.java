package me.spaff.buildbattle.server.game;

import me.spaff.buildbattle.player.BBPlayer;
import org.bukkit.Location;
import org.bukkit.Material;

public class Plot {
    private final Location boundaryA;
    private final Location boundaryB;

    private BBPlayer owner;
    private int score;

    public Plot(Location boundaryA, Location boundaryB) {
        this.boundaryA = boundaryA;
        this.boundaryB = boundaryB;
    }

    // Owner
    public BBPlayer getOwner() {
        return owner;
    }

    public void setOwner(BBPlayer bbPlayer) {
        this.owner = bbPlayer;
    }

    // Score
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    // Boundaries
    public Location getBoundaryA() {
        return boundaryA;
    }

    public Location getBoundaryB() {
        return boundaryB;
    }

    public Location getCenter() {
        double centerX = (Math.min(boundaryA.getX(), boundaryB.getX()) + Math.max(boundaryA.getX(), boundaryB.getX())) / 2;
        double centerY = (Math.min(boundaryA.getY(), boundaryB.getY()) + Math.max(boundaryA.getY(), boundaryB.getY())) / 2;
        double centerZ = (Math.min(boundaryA.getZ(), boundaryB.getZ()) + Math.max(boundaryA.getZ(), boundaryB.getZ())) / 2;
        return new Location(boundaryA.getWorld(), centerX, centerY, centerZ);
    }

    public Location getBottomCenter() {
        return new Location(boundaryA.getWorld(), getCenter().getX(), Math.min(boundaryA.getY(), boundaryB.getY()) + 1, getCenter().getZ());
    }

    public void fillFloor(Material block) {
        for (int x = (int)Math.min(boundaryA.getX(), boundaryB.getX()); x <= (int)Math.max(boundaryA.getX(), boundaryB.getX()); x++) {
            for (int z = (int)Math.min(boundaryA.getZ(), boundaryB.getZ()); z <= (int)Math.max(boundaryA.getZ(), boundaryB.getZ()); z++) {
                boundaryA.getWorld().getBlockAt(x, (int)Math.min(boundaryA.getY(), boundaryB.getY()), z).setType(block);
            }
        }
    }
}