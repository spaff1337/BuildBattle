package me.spaff.buildbattle.server.game;

import me.spaff.buildbattle.Constants;
import me.spaff.spflib.file.FileManager;
import me.spaff.spflib.logger.SLevel;
import me.spaff.spflib.logger.SLogger;
import me.spaff.spflib.utils.FileUtils;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class GameData {
    private Location waitingLobby;
    private List<Plot> plots = new ArrayList<>();

    public void loadData() {
        boolean wasGameSetup = FileManager.isFileInDirectory(FileManager.getPluginDirectory(), "arena");
        if (!wasGameSetup) {
            SLogger.log(SLevel.WARNING, "Game was not configured for this server yet!");
            return;
        }

        waitingLobby = FileUtils.getLocationFromFile("arena", "waiting-location");

        for (int i = 1; i <= Constants.MAX_PLAYERS; i++) {
            Location plotBoundaryA = FileUtils.getLocationFromFile("arena", "plots." + i + ".a");
            Location plotBoundaryB = FileUtils.getLocationFromFile("arena", "plots." + i + ".b");

            System.out.println("plotBoundaryA: " + plotBoundaryA);
            System.out.println("plotBoundaryB: " + plotBoundaryB);

            if (plotBoundaryA != null && plotBoundaryB != null)
                plots.add(new Plot(plotBoundaryA, plotBoundaryB));
            else
                SLogger.log(SLevel.SEVERE, "A plot location failed to load!");
        }
    }

    public Location getWaitingLocation() {
        return waitingLobby;
    }

    public List<Plot> getPlots() {
        return plots;
    }
}